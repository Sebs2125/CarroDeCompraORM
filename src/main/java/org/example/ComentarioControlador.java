package org.example;

import io.javalin.http.Context;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class ComentarioControlador {

    private final ComentarioGestion comentarioGestion = ComentarioGestion.getInstance();
    private final WebSocketGestion  wsGestion         = WebSocketGestion.getInstance();
    private final ProductoGestion   productoGestion    = ProductoGestion.getInstance();
    private final Gson gson = new Gson();


    public void mostrarDetalleProducto(Context ctx) {

        Long id = Long.parseLong(ctx.pathParam("id"));

        Producto producto = productoGestion.obtenerProductoPorId(id).orElse(null);
        if (producto == null) {
            ctx.status(404).result("Producto no encontrado");
            return;
        }

        Map<String, Object> modelo = new HashMap<>();
        modelo.put("producto",    producto);
        // Los comentarios vienen de la relacion @OneToMany de Hibernate
        modelo.put("comentarios", producto.getComentarios());
        modelo.put("usuario",     ctx.sessionAttribute("usuarioActual"));
        modelo.put("esAdmin",
                ctx.sessionAttribute("usuarioActual") != null
                        && ((Usuario) ctx.sessionAttribute("usuarioActual")).isConfirmarAdmin());

        ctx.render("/templates/producto_detalle.html", modelo);
    }


    public void agregarComentario(Context ctx) {

        Long productoId = Long.parseLong(ctx.pathParam("id"));
        Usuario usuario = ctx.sessionAttribute("usuarioActual");

        if (usuario == null) {
            ctx.status(401).result("Debe iniciar sesion para comentar");
            return;
        }

        String texto = ctx.formParam("texto");
        if (texto == null || texto.isBlank()) {
            ctx.status(400).result("El comentario no puede estar vacio");
            return;
        }

        // Obtener la entidad Producto para la relacion JPA
        Producto producto = productoGestion.obtenerProductoPorId(productoId).orElse(null);
        if (producto == null) {
            ctx.status(404).result("Producto no encontrado");
            return;
        }

        // CAMBIO: constructor JPA new Comentario(Producto, Usuario, String)
        Comentario comentario = new Comentario(producto, usuario, texto.trim());

        // Persistir en BD via ProductoGestion (Hibernate)
        productoGestion.agregarComentario(comentario);

        // Registrar en cache en memoria para WS
        comentarioGestion.agregarComentario(comentario);

        System.out.println("Comentario agregado por " + usuario.getUsuario()
                + " en producto " + productoId);

        // Broadcast WebSocket a todos los que ven ese producto
        broadcastNuevoComentario(productoId.intValue(), comentario);

        ctx.redirect("/productos/" + productoId);
    }

    public void eliminarComentario(Context ctx) {
        Usuario usuario = ctx.sessionAttribute("usuarioActual");
        if (usuario == null || !usuario.isConfirmarAdmin()) {
            ctx.status(403).result("Acceso denegado");
            return;
        }

        // CAMBIO: Long para coincidir con ProductoGestion.eliminarComentario(Long)
        Long comentarioId = Long.parseLong(ctx.pathParam("id"));

        // Eliminar del cache en memoria y obtener el productoId para el broadcast
        int productoId = comentarioGestion.eliminarComentario(comentarioId);

        // Si no estaba en cache, buscar el productoId directamente en BD
        // antes de eliminarlo
        if (productoId == -1) {
            // Intentar obtener productoId desde la BD antes de borrar
            // (el productoId se recupera del comentario antes de eliminarlo)
            try {
                // Buscar el comentario para saber a qué producto pertenece
                // Se elimina por ProductoGestion que hace el find antes del remove
                productoGestion.eliminarComentario(comentarioId);
            } catch (Exception e) {
                ctx.status(404).result("Comentario no encontrado");
                return;
            }
            ctx.redirect("/admin");
            return;
        }

        // Eliminar de la BD
        productoGestion.eliminarComentario(comentarioId);

        // WebSocket: notificar en tiempo real
        wsGestion.broadcastCommentDeleted(productoId, comentarioId.intValue());

        System.out.println("Admin elimino comentario ID: " + comentarioId
                + " del producto: " + productoId);

        String referer = ctx.header("Referer");
        if (referer != null && referer.contains("/admin")) {
            ctx.redirect("/admin");
        } else {
            ctx.redirect("/productos/" + productoId);
        }
    }

    private void broadcastNuevoComentario(int productoId, Comentario comentario) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("type",      "comment_added");
        msg.put("commentId", comentario.getId());
        msg.put("productId", productoId);
        msg.put("autor",     comentario.getAutor());   // getAutor() devuelve usuario.getNombre()
        msg.put("texto",     comentario.getTexto());
        msg.put("fecha",     comentario.getFecha());
        String json = gson.toJson(msg);
        wsGestion.broadcastComentarioAgregado(productoId, json);
    }
}