package org.example;

import io.javalin.http.Context;
import java.util.HashMap;
import java.util.Map;

public class ProductoControlador
{
    private ProductoGestion productoGestion = ProductoGestion.getInstance();

    // Lista con paginación (Punto 8)
    public void mostrarProducto(Context ctx)
    {

        int pagina = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
        int tamano = 10;

        long totalProductos = productoGestion.contarProductos();
        int totalPaginas = (int) Math.ceil((double) totalProductos / tamano);

        if (pagina < 1) pagina = 1;
        if (pagina > totalPaginas && totalPaginas > 0) pagina = totalPaginas;

        Map<String, Object> modelo = new HashMap<>();
        modelo.put("productos", productoGestion.obtenerProductosPaginados(pagina, tamano));
        modelo.put("paginaActual", pagina);
        modelo.put("totalPaginas", totalPaginas);
        modelo.put("hayAnterior", pagina > 1);
        modelo.put("haySiguiente", pagina < totalPaginas);

        Usuario usuarioActual = ctx.sessionAttribute("usuarioActual");
        modelo.put("usuario", usuarioActual);

        // Contador del carrito
        CarritoDeCompra carro = ctx.sessionAttribute("carro");
        modelo.put("cantidadCarro", carro != null ? carro.getCantidadProductos() : 0);

        ctx.render("/templates/productos.html", modelo);
    }

    // Vista detalle de producto con comentarios (Punto 6)
    public void mostrarDetalleProducto(Context ctx)
    {
        Long productoId = Long.parseLong(ctx.pathParam("id"));

        Producto producto = productoGestion.obtenerProductoPorId(productoId).orElse(null);

        if (producto == null)
        {
            ctx.status(404).result("Producto no encontrado");
            return;
        }

        Map<String, Object> modelo = new HashMap<>();
        modelo.put("producto", producto);
        modelo.put("imagenes", producto.getImagenes());
        modelo.put("comentarios", producto.getComentarios());

        Usuario usuarioActual = ctx.sessionAttribute("usuarioActual");
        modelo.put("usuario", usuarioActual);
        modelo.put("esAdmin", usuarioActual != null && usuarioActual.isConfirmarAdmin());

        ctx.render("/templates/producto-detalle.html", modelo);
    }

    public void mostrarAdminPanel(Context ctx)
    {
        Usuario usuarioActual = ctx.sessionAttribute("usuarioActual");

        if (usuarioActual == null || !usuarioActual.isConfirmarAdmin())
        {
            ctx.status(403).result("Acceso denegado al panel admin");
            return;
        }

        Map<String, Object> modelo = new HashMap<>();
        modelo.put("productos", productoGestion.getListaProductos());
        modelo.put("usuario", usuarioActual);
        ctx.render("/templates/admin.html", modelo);
    }

    public void crearProducto(Context ctx)
    {
        Usuario usuarioActual = ctx.sessionAttribute("usuarioActual");

        if (usuarioActual == null || !usuarioActual.isConfirmarAdmin())
        {
            ctx.status(403).result("Acceso denegado");
            return;
        }

        try
        {
            String nombre = ctx.formParam("nombre");
            String descripcion = ctx.formParam("descripcion");
            Double precio = Double.parseDouble(ctx.formParam("precio"));
            int inventario = Integer.parseInt(ctx.formParam("inventario"));

            Producto producto = new Producto(nombre, descripcion, precio, inventario);

            // Procesar imagen (obligatoria)
            var imagen = ctx.uploadedFile("imagen");

            if (imagen == null || imagen.size() == 0)
            {
                ctx.status(400).result("La imagen es obligatoria");
                return;
            }

            byte[] bytes = imagen.content().readAllBytes();
            String base64 = java.util.Base64.getEncoder().encodeToString(bytes);
            producto.agregarImagen(new ProductoImagen(producto, base64));

            productoGestion.addProducto(producto);
            ctx.redirect("/admin");

        } catch (Exception e)
        {
            ctx.status(400).result("Error al crear producto: " + e.getMessage());
        }
    }

    public void actualizarProducto(Context ctx)
    {
        Usuario usuarioActual = ctx.sessionAttribute("usuarioActual");

        if (usuarioActual == null || !usuarioActual.isConfirmarAdmin())
        {
            ctx.status(403).result("Acceso denegado");
            return;
        }

        try
        {
            Long id = Long.parseLong(ctx.pathParam("id"));
            String nombre = ctx.formParam("nombre");
            String descripcion = ctx.formParam("descripcion");
            Double precio = Double.parseDouble(ctx.formParam("precio"));
            int inventario = Integer.parseInt(ctx.formParam("inventario"));

            Producto producto = new Producto(nombre, descripcion, precio, inventario);
            producto.setId(id);

            // Imagen opcional en actualización
            var imagen = ctx.uploadedFile("imagen");

            if (imagen != null && imagen.size() > 0)
            {
                byte[] bytes = imagen.content().readAllBytes();
                String base64 = java.util.Base64.getEncoder().encodeToString(bytes);
                producto.agregarImagen(new ProductoImagen(producto, base64));
            }

            productoGestion.actualizarProducto(producto);
            ctx.redirect("/admin");

        } catch (Exception e)
        {
            ctx.status(400).result("Error al actualizar: " + e.getMessage());
        }
    }

    public void deletearProducto(Context ctx)
    {
        Usuario usuarioActual = ctx.sessionAttribute("usuarioActual");

        if (usuarioActual == null || !usuarioActual.isConfirmarAdmin())
        {
            ctx.status(403).result("Acceso denegado");
            return;
        }

        try
        {
            Long id = Long.parseLong(ctx.pathParam("id"));
            productoGestion.deletearProducto(id);
            ctx.redirect("/admin");
        } catch (Exception e)
        {
            ctx.status(400).result("Error al eliminar: " + e.getMessage());
        }
    }
}