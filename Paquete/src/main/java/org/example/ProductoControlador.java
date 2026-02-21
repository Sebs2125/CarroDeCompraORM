package org.example;

import io.javalin.http.Context;

import java.util.HashMap;
import java.util.Map;

public class ProductoControlador {
    private ProductoGestion productoGestion = ProductoGestion.getInstance();

    public void mostrarProducto(Context ctx) // Punto #4: Los usuarios no autenticados no tendrán acceso a la lista de
                                             // productos.
    {
        Map<String, Object> modelo = new HashMap<>();
        modelo.put("productos", productoGestion.getListaProductos());

        Usuario usuarioActual = ctx.sessionAttribute("usuarioActual");
        modelo.put("usuario", usuarioActual);

        CarritoDeCompra carro = ctx.sessionAttribute("carro");

        if (carro != null) {
            modelo.put("Cantidad de items en el carro: ", carro.getCantidadProductos());
        } else {
            modelo.put("Cantidad de Items en el carro: ", 0);
        }

        ctx.render("/templates/productos.html", modelo);

    }

    public void mostrarAdminPanel(Context ctx) // Punto #1 completo hacia abajo: Panel de admin solo es accesible para
                                               // administradores.
    {
        Usuario usuarioActual = ctx.sessionAttribute("usuarioActual");

        if (usuarioActual == null || !usuarioActual.isConfirmarAdmin()) {
            ctx.status(403).result("Acceso denegado al panel admin");
            return;
        }

        Map<String, Object> modelo = new HashMap<>();
        modelo.put("productos", productoGestion.getListaProductos());
        modelo.put("usuario", usuarioActual);
        ctx.render("/templates/admin.html", modelo);
    }

    public void crearProducto(Context ctx) // Crear el producto.
    {
        Usuario usuarioActual = ctx.sessionAttribute("usuarioActual");

        if (usuarioActual == null || !usuarioActual.isConfirmarAdmin()) {
            ctx.status(403).result("Acceso denegado al panel admin");
            return;
        }

        try {
            String nombre = ctx.formParam("nombre");
            Double precio = new Double(ctx.formParam("precio"));
            int inventario = new Integer(ctx.formParam("inventario"));

            Producto producto = new Producto(nombre, precio, inventario);
            productoGestion.addProducto(producto);

            System.out.println("Producto creado por " + usuarioActual.getUsuario() + ":" + producto.getNombre());
            ctx.redirect("/admin");
        } catch (Exception e) {
            System.err.println("Error al crear producto: " + e.getMessage());
            ctx.status(400).result("Error al crear producto");
        }

    }

    public void actualizarProducto(Context ctx) // Actualizar Producto.
    {
        Usuario usuarioActual = ctx.sessionAttribute("usuarioActual");

        if (usuarioActual == null || !usuarioActual.isConfirmarAdmin()) {
            ctx.status(403).result("Acceso denegado al panel admin");
            return;
        }

        try {
            int id = Integer.parseInt(ctx.formParam("id"));
            String nombre = ctx.formParam("nombre");
            Double precio = new Double(ctx.formParam("precio"));
            int inventario = Integer.parseInt(ctx.formParam("inventario"));

            Producto producto = new Producto(nombre, precio, inventario);
            producto.setId(id);
            productoGestion.actualizarProducto(producto);

            System.out.println("Producto modificado por " + usuarioActual.getUsuario() + ":" + producto.getNombre());
            ctx.redirect("/admin");

        } catch (Exception e) {
            System.err.println("Error al actualizar producto: " + e.getMessage());
            ctx.status(400).result("Error al actualizar producto");
        }

    }

    public void deletearProducto(Context ctx) // Eliminar producto
    {
        Usuario usuarioActual = ctx.sessionAttribute("usuarioActual");

        if (usuarioActual == null || !usuarioActual.isConfirmarAdmin()) {
            ctx.status(403).result("Acceso denegado al panel admin");
            return;
        }

        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            productoGestion.deletearProducto(id);

            System.out.println("Producto eliminado por " + usuarioActual.getUsuario() + ":" + id);
            ctx.redirect("/admin");
        } catch (Exception e) {
            System.err.println("Error al eliminar producto: " + e.getMessage());
            ctx.status(400).result("Error al eliminar producto");
        }

    }

}
