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
        modelo.put("comentarios", producto.getComentarios());
        modelo.put("usuario",     ctx.sessionAttribute("usuarioActual"));
        modelo.put("esAdmin",
                ctx.sessionAttribute("usuarioActual") != null
                        && ((Usuario) ctx.sessionAttribute("usuarioActual")).isConfirmarAdmin());

        // CORRECCION: nombre de template usa guion, no underscore
        ctx.render("/templates/producto-detalle.html", modelo);
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

        Producto producto = productoGestion.obtenerProductoPorId(productoId).orElse(null);
        if (producto == null) {
            ctx.status(404).result("Producto no encontrado");
            return;
        }

        Comentario comentario = new Comentario(producto, usuario, texto.trim());

        productoGestion.agregarComentario(comentario);
        comentarioGestion.agregarComentario(comentario);

        System.out.println("Comentario agregado por " + usuario.getUsuario()
                + " en producto " + productoId);

        broadcastNuevoComentario(productoId.intValue(), comentario);

        ctx.redirect("/productos/" + productoId);
    }

    public void eliminarComentario(Context ctx) {
        Usuario usuario = ctx.sessionAttribute("usuarioActual");
        if (usuario == null || !usuario.isConfirmarAdmin()) {
            ctx.status(403).result("Acceso denegado");
            return;
        }

        Long comentarioId = Long.parseLong(ctx.pathParam("id"));

        int productoId = comentarioGestion.eliminarComentario(comentarioId);

        if (productoId == -1) {
            try {
                productoGestion.eliminarComentario(comentarioId);
            } catch (Exception e) {
                ctx.status(404).result("Comentario no encontrado");
                return;
            }
            ctx.redirect("/admin");
            return;
        }

        productoGestion.eliminarComentario(comentarioId);
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
        msg.put("autor",     comentario.getAutor());
        msg.put("texto",     comentario.getTexto());
        msg.put("fecha",     comentario.getFecha());
        String json = gson.toJson(msg);
        wsGestion.broadcastComentarioAgregado(productoId, json);
    }
}