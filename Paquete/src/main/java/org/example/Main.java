package org.example;

import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinThymeleaf;

/*
    Aplicación de Carrito de Compras en Sesión.
    En esta clase se implementan los puntos del 1 al 6 de la practica CRUD.
 */
//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    public static void main(String[] args) {
        UsuarioGestion usuarioGestion = UsuarioGestion.getInstance(); // Gestiones (servicios)
        ProductoGestion productoGestion = ProductoGestion.getInstance();

        AutenticarUsuarioControlador autenticarUsuarioControlador = new AutenticarUsuarioControlador();
        ProductoControlador productoControlador = new ProductoControlador();
        CarroControlador carroControlador = new CarroControlador();

        Javalin app = Javalin.create(set -> {
            set.staticFiles.add("/public", Location.CLASSPATH);
            set.fileRenderer(new JavalinThymeleaf());
        }).start(7000);

        System.out.println("Carrito de Comrpa en Sesion");

        app.get("/", ctx -> ctx.redirect("/productos")); // Autenticar
        app.get("/login", autenticarUsuarioControlador::mostrarPaginaLogin);
        app.post("/login", autenticarUsuarioControlador::login);
        app.get("/logout", autenticarUsuarioControlador::logout);

        app.get("/productos", productoControlador::mostrarProducto);

        app.get("/admin", productoControlador::mostrarAdminPanel);
        app.post("/admin/productos", productoControlador::crearProducto);
        app.post("/admin/productos/{id}/update", productoControlador::actualizarProducto);
        app.post("/admin/productos/{id}/delete", productoControlador::deletearProducto);

        app.post("/carrito/add", carroControlador::addAlCarro);
        app.get("/carrito", carroControlador::mostrarCarro);
        app.post("/carrito/limpiar", carroControlador::limpiarCarro);

        app.error(404, ctx -> {
            ctx.result("Pagina no encontrada - 404");
        });

        app.exception(Exception.class, (e, ctx) -> {
            ctx.status(500).result("Error del servidor - 500");
            e.printStackTrace();
        });

        System.out.println("Usuarios cargados: " + usuarioGestion.todosLosUsuarios().size());
        System.out.println("Productos cargados: " + productoGestion.getListaProductos().size());
        System.out.println("Servidor HTTP fue iniciado en el puerto 7000");
        System.out.println("Sistema listo para recibir las peticiones del usuario \n");

    }
}
