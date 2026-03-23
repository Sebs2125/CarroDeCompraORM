package org.example;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinThymeleaf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {

    private static List<Venta> ventas = new ArrayList<>();
    private static long ventaIdCounter = 1;

    public static void main(String[] args) {

        // Iniciar H2 servidor
        //HibernateConsulta.iniciarServidor();

        final UsuarioGestion usuarioGestion = UsuarioGestion.getInstance();
        final ProductoGestion productoGestion = ProductoGestion.getInstance();
        final AutenticarUsuarioControlador autenticarUsuarioControlador = new AutenticarUsuarioControlador();
        final ProductoControlador productoControlador = new ProductoControlador();
        final CarroControlador carroControlador = new CarroControlador();
        final ComentarioControlador comentarioControlador = new ComentarioControlador();

        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public", Location.CLASSPATH);
            config.fileRenderer(new JavalinThymeleaf());
        }).start(8080);

        // Middleware: Validar cookie "recordar usuario" (Punto 4)
        app.before(ctx -> {
            if ( ctx.sessionAttribute("usuarioActual") == null )
            {
                String token = ctx.cookie("recordarUsuario");

                if ( token != null )
                {
                    String username = ServicioCookies.validaryExtraerUsuario(token);

                    if ( username != null )
                    {
                        Usuario user = usuarioGestion.buscarUsuario(username).orElse(null );
                        if ( user != null )
                        {
                            ctx.sessionAttribute("usuarioActual", user);
                            System.out.println("Auto-login via cookie: " + username);
                        }
                    }
                }
            }
        });

        // Rutas de autenticación
        app.get("/", ctx -> ctx.redirect("/productos"));
        app.get("/login", autenticarUsuarioControlador::mostrarPaginaLogin);
        app.post("/login", autenticarUsuarioControlador::login);
        app.get("/logout", autenticarUsuarioControlador::logout);

        // Rutas de productos (con paginación - Punto 8)
        app.get("/productos", productoControlador::mostrarProducto);
        app.get("/producto/{id}", productoControlador::mostrarDetalleProducto); // Punto 6

        // Comentarios (Punto 6 y 7)
        app.post("/comentarios", comentarioControlador::agregarComentario);
        app.post("/comentarios/{id}/delete", comentarioControlador::eliminarComentario);

        // Ventas
        app.before("/ventas", ctx -> {
            if (ctx.sessionAttribute("usuarioActual") == null) {
                ctx.redirect("/login");
            }
        });

        app.get("/ventas", ctx -> {
            Map<String, Object> modelo = new HashMap<>();
            modelo.put("ventas", ventas);
            modelo.put("usuario", ctx.sessionAttribute("usuarioActual"));
            ctx.render("/templates/ventas.html", modelo);
        });

        // Admin
        app.get("/admin", productoControlador::mostrarAdminPanel);
        app.post("/admin/productos", productoControlador::crearProducto);
        app.post("/admin/productos/{id}/update", productoControlador::actualizarProducto);
        app.post("/admin/productos/{id}/delete", productoControlador::deletearProducto);

        // Carrito
        app.get("/carrito", carroControlador::mostrarCarro);
        app.post("/carrito/add", carroControlador::addAlCarro);
        app.post("/carrito/limpiar", carroControlador::limpiarCarro);

        app.post("/carrito/procesar", ctx -> {
            CarritoDeCompra carro = ctx.sessionAttribute("carro");
            String nombreCliente = ctx.formParam("nombreCliente");

            if ( carro == null || carro.getListaProductos().isEmpty() )
            {
                ctx.redirect("/carrito");
                return;
            }

            Venta venta = new Venta(ventaIdCounter++, nombreCliente, carro.getListaProductos());
            ventas.add(venta);
            carro.limpiar();
            ctx.redirect("/ventas");
        });

        app.error(404, ctx -> ctx.result("Pagina no encontrada - 404"));
        app.exception(Exception.class, (e, ctx) -> {
            ctx.status(500).result("Error: " + e.getMessage());
            e.printStackTrace();
        });

        System.out.println("Usuarios: " + usuarioGestion.todosLosUsuarios().size());
        System.out.println("Productos: " + productoGestion.getListaProductos().size());
        System.out.println("Servidor: http://localhost:8080");
        System.out.println("H2 Console: http://localhost:8082");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            //HibernateConsulta.detenerServidor();
            productoGestion.shutdown();
        }));

        app = Javalin.create(config -> {
            config.http.defaultContentType = "text/html; charset=UTF-8";
        });
    }


}