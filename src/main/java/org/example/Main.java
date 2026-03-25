package org.example;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinThymeleaf;
import com.google.gson.Gson;
import org.h2.tools.Server;

import java.util.HashMap;
import java.util.Map;

/**
 * Aplicación de Carrito de Compras con WebSockets.
 *
 * Requerimientos implementados:
 *  1. Base HTTP/CRUD (heredado)
 *  2. Contador de usuarios logueados en tiempo real (WS)
 *  3. Eliminación de comentarios en tiempo real (WS)
 *  4. Dashboard admin con ventas y gráfico de torta en tiempo real (WS)
 *  5. Docker / Docker Compose (heredado + actualizado)
 */
public class Main {

    public static void main(String[] args)
    {
        try {
            Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "9092").start();
            System.out.println("Servidor H2 iniciado en el puerto 9092");
        } catch (Exception e) {
            System.err.println("No se pudo iniciar el servidor H2: " + e.getMessage());
        }

        // ── Servicios (Singleton) ────────────────────────────────────────────
        UsuarioGestion usuarioGestion = UsuarioGestion.getInstance();
        ProductoGestion productoGestion = ProductoGestion.getInstance();
        ComentarioGestion comentarioGestion = ComentarioGestion.getInstance();
        VentaGestion ventaGestion = VentaGestion.getInstance();
        WebSocketGestion wsGestion = WebSocketGestion.getInstance();

        // ── Controladores HTTP ───────────────────────────────────────────────
        AutenticarUsuarioControlador autenticarCtrl = new AutenticarUsuarioControlador();
        ProductoControlador productoCtrl = new ProductoControlador();
        CarroControlador carroCtrl = new CarroControlador();
        ComentarioControlador comentarioCtrl = new ComentarioControlador();

        Gson gson = new Gson();

        // ── Javalin ──────────────────────────────────────────────────────────
        // WebSocket está incluido en Javalin 5 / Jetty sin configuración extra.
        Javalin app = Javalin.create(set -> {
            set.staticFiles.add("/public", Location.CLASSPATH);
            set.fileRenderer(new JavalinThymeleaf());
        }).start(8080);

        System.out.println("=== Carrito de Compra con WebSockets ===");

        // ════════════════════════════════════════════════════════════════════
        //  RUTAS HTTP
        // ════════════════════════════════════════════════════════════════════

        app.get("/", ctx -> ctx.redirect("/productos"));

        // Auth
        app.get("/login",  autenticarCtrl::mostrarPaginaLogin);
        app.post("/login",  autenticarCtrl::login);
        app.get("/logout", autenticarCtrl::logout);

        // Productos
        app.get("/productos", productoCtrl::mostrarProducto);
        app.get("/productos/{id}", comentarioCtrl::mostrarDetalleProducto);

        // Comentarios
        app.post("/productos/{id}/comentarios", comentarioCtrl::agregarComentario);
        app.post("/admin/comentarios/{id}/delete", comentarioCtrl::eliminarComentario);

        // Carrito
        app.get("/carrito",       carroCtrl::mostrarCarro);
        app.post("/carrito/add",  carroCtrl::addAlCarro);
        app.post("/carrito/limpiar", carroCtrl::limpiarCarro);

        // Procesar compra → registra la venta y notifica dashboard via WS
        app.post("/carrito/procesar", ctx -> {
            CarritoDeCompra carro = ctx.sessionAttribute("carro");
            Usuario usuario = ctx.sessionAttribute("usuarioActual");

            if (carro != null && !carro.getListaProductos().isEmpty()) {
                String nombreCliente = ctx.formParam("nombreCliente");
                if (nombreCliente == null || nombreCliente.isBlank()) {
                    nombreCliente = (usuario != null) ? usuario.getNombre() : "Invitado";
                }
                // ── Registrar venta (también hace broadcast WS al dashboard) ──
                ventaGestion.registrarVenta(nombreCliente, carro.getListaProductos());
                carro.getListaProductos().clear();
            }

            ctx.redirect("/ventas");
        });

        // Ventas (requiere login)
        app.before("/ventas", ctx -> {
            if (ctx.sessionAttribute("usuarioActual") == null) {
                ctx.redirect("/login");
            }
        });
        app.get("/ventas", ctx -> {
            Map<String, Object> modelo = new HashMap<>();
            modelo.put("ventas", ventaGestion.getVentas());
            modelo.put("usuario", ctx.sessionAttribute("usuarioActual"));
            ctx.render("/templates/ventas.html", modelo);
        });

        // Admin CRUD productos
        app.get("/admin", productoCtrl::mostrarAdminPanel);
        app.post("/admin/productos", productoCtrl::crearProducto);
        app.post("/admin/productos/{id}/update", productoCtrl::actualizarProducto);
        app.post("/admin/productos/{id}/delete", productoCtrl::deletearProducto);

        // Dashboard (solo admin)
        app.get("/dashboard", ctx -> {
            Usuario usuario = ctx.sessionAttribute("usuarioActual");
            if (usuario == null || !usuario.isConfirmarAdmin()) {
                ctx.status(403).result("Acceso denegado al dashboard");
                return;
            }
            Map<String, Object> modelo = new HashMap<>();
            modelo.put("usuario", usuario);
            modelo.put("dashboardData", gson.toJson(ventaGestion.getDashboardData()));
            ctx.render("/templates/dashboard.html", modelo);
        });

        // ════════════════════════════════════════════════════════════════════
        //  WEBSOCKETS
        // ════════════════════════════════════════════════════════════════════

        /**
         * WS /ws/users  → canal del contador de usuarios en línea.
         * Cualquier página puede conectarse para ver cuántos usuarios hay.
         */
        app.ws("/ws/users", ws -> {
            ws.onConnect(ctx -> {
                wsGestion.subscribeUserCount(ctx);
                System.out.println("WS users connect: " + ctx.getSessionId());
            });
            ws.onMessage(ctx -> {
                // El cliente puede mandar su estado de login al conectarse
                // Mensaje esperado: {"action":"login","username":"...","httpSessionId":"..."}
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, String> msg = gson.fromJson(ctx.message(), Map.class);
                    String action = msg.get("action");
                    String username = msg.get("username");
                    if ("login".equals(action) && username != null) {
                        wsGestion.userLoggedIn(ctx.getSessionId(), username);
                    } else if ("logout".equals(action)) {
                        wsGestion.userLoggedOut(ctx.getSessionId());
                    }
                } catch (Exception e) {
                    System.err.println("WS users message error: " + e.getMessage());
                }
            });
            ws.onClose(ctx -> {
                wsGestion.userLoggedOut(ctx.getSessionId());
                wsGestion.unsubscribeUserCount(ctx);
                wsGestion.cleanupSession(ctx.getSessionId());
                System.out.println("WS users close: " + ctx.getSessionId());
            });
            ws.onError(ctx -> System.err.println("WS users error: " + ctx.error()));
        });

        /**
         * WS /ws/comments/{productId} → canal de comentarios por producto.
         * Sólo las páginas de detalle de ese producto se conectan aquí.
         */
        app.ws("/ws/comments/{productId}", ws -> {
            ws.onConnect(ctx -> {
                int productId = Integer.parseInt(ctx.pathParam("productId"));
                wsGestion.subscribeComments(ctx, productId);
                System.out.println("WS comments connect: producto " + productId);
            });
            ws.onClose(ctx -> {
                int productId = Integer.parseInt(ctx.pathParam("productId"));
                wsGestion.unsubscribeComments(ctx, productId);
                wsGestion.cleanupSession(ctx.getSessionId());
            });
            ws.onError(ctx -> System.err.println("WS comments error: " + ctx.error()));
        });

        /**
         * WS /ws/dashboard → canal del dashboard de ventas.
         * Solo el dashboard admin se conecta aquí.
         */
        app.ws("/ws/dashboard", ws -> {
            ws.onConnect(ctx -> {
                wsGestion.subscribeDashboard(ctx);
                System.out.println("WS dashboard connect: " + ctx.getSessionId());
            });
            ws.onClose(ctx -> {
                wsGestion.unsubscribeDashboard(ctx);
                wsGestion.cleanupSession(ctx.getSessionId());
            });
            ws.onError(ctx -> System.err.println("WS dashboard error: " + ctx.error()));
        });

        // ── Errores globales ────────────────────────────────────────────────
        app.error(404, ctx -> ctx.result("Página no encontrada - 404"));
        app.exception(Exception.class, (e, ctx) -> {
            ctx.status(500).result("Error del servidor: " + e.getMessage());
            e.printStackTrace();
        });

        // ── Info de inicio ──────────────────────────────────────────────────
        System.out.println("Usuarios cargados: " + usuarioGestion.todosLosUsuarios().size());
        System.out.println("Productos cargados: " + productoGestion.getListaProductos().size());
        System.out.println("Servidor HTTP/WS iniciado en el puerto 8080");
        System.out.println("WebSockets disponibles:");
        System.out.println("  ws://localhost:8080/ws/users");
        System.out.println("  ws://localhost:8080/ws/comments/{productId}");
        System.out.println("  ws://localhost:8080/ws/dashboard");
        System.out.println("Sistema listo.\n");
    }
}