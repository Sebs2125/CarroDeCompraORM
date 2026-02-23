package org.example;

import io.javalin.Javalin;

import io.javalin.http.staticfiles.Location;

import io.javalin.rendering.template.JavalinThymeleaf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*

    Aplicación de Carrito de Compras en Sesión.

    En esta clase se implementan los puntos del 1 al 6 de la practica CRUD.

 */

public class Main
{

    private static List<Venta> ventas = new ArrayList<>();
    private static long ventaIdCounter = 1;

    public static void main(String[] args)
    {

        UsuarioGestion usuarioGestion = UsuarioGestion.getInstance(); // Gestiones (servicios)

        ProductoGestion productoGestion = ProductoGestion.getInstance();

        AutenticarUsuarioControlador autenticarUsuarioControlador = new AutenticarUsuarioControlador();

        ProductoControlador productoControlador = new ProductoControlador();

        CarroControlador carroControlador = new CarroControlador();

        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public", Location.CLASSPATH);
            config.fileRenderer( new JavalinThymeleaf());
        }).start(8080);

        System.out.println("Carrito de Comrpa en Sesion");

        app.get("/", ctx -> ctx.redirect("/productos")); // Autenticar

        app.get("/login", autenticarUsuarioControlador::mostrarPaginaLogin);

        app.post("/login", autenticarUsuarioControlador::login);

        app.get("/logout", autenticarUsuarioControlador::logout);

        app.get("/productos", productoControlador::mostrarProducto);

        app.before("/ventas", ctx -> {
            if (ctx.sessionAttribute("usuarioActual") == null) {
                ctx.redirect("/login");
            }
        });

        app.get("/ventas", ctx -> {
            Map<String, Object> modelo = new HashMap<>();
            modelo.put("ventas", ventas);

            Usuario usuarioActual = ctx.sessionAttribute("usuarioActual");
            modelo.put("usuario", usuarioActual);

            ctx.render("/templates/ventas.html", modelo);

        });

        app.get("/admin", productoControlador::mostrarAdminPanel);

        app.post("/admin/productos", productoControlador::crearProducto);

        app.post("/admin/productos/{id}/update", productoControlador::actualizarProducto);

        app.post("/admin/productos/{id}/delete", productoControlador::deletearProducto);

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

           if ( nombreCliente == null || nombreCliente.trim().isEmpty() )
           {
               ctx.status(400).result("Nombre de cliente requerido");
               return;
           }

           Venta venta = new Venta(ventaIdCounter++, nombreCliente, carro.getListaProductos());
           ventas.add(venta);

           System.out.println("Venta realizada: ID=" +venta.getId() +
                   ", Cliente=" + nombreCliente +
                   ", Total=" + venta.getTotal());
           carro.limpiar();
           ctx.redirect("/ventas");

        });

        app.error(404, ctx -> ctx.result("Pagina no encontrada - 404"));

        app.exception(Exception.class, (e, ctx) -> {
            ctx.status(500).result("Error del servidor - 500: " + e.getMessage());
            e.printStackTrace();
        });

        System.out.println("Usuarios cargados: " + usuarioGestion.todosLosUsuarios().size());

        System.out.println("Productos cargados: " + productoGestion.getListaProductos().size());

        System.out.println("Servidor HTTP fue iniciado en el puerto 8080");

        System.out.println("Sistema listo para recibir las peticiones del usuario \n");

    }

}