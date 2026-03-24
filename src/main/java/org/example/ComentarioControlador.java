package org.example;

import io.javalin.http.Context;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class ComentarioControlador {

    private final ComentarioGestion comentarioGestion = ComentarioGestion.getInstance();
    private final WebSocketGestion wsGestion = WebSocketGestion.getInstance();
    private final ProductoGestion productoGestion = ProductoGestion.getInstance();
    private final Gson gson = new Gson();

    /** GET /productos/{id} → muestra el detalle del producto con sus comentarios */
    public void mostrarDetalleProducto(Context ctx) {
        int id = Integer.parseInt(ctx.pathParam("id"));

        Producto producto = productoGestion.obtenerProductoPorId(id).orElse(null);
        if (producto == null) {
            ctx.status(404).result("Producto no encontrado");
            return;
        }

        Map<String, Object> modelo = new HashMap<>();
        modelo.put("producto", producto);
        modelo.put("comentarios", comentarioGestion.getComentariosPorProducto(id));
        modelo.put("usuario", ctx.sessionAttribute("usuarioActual"));

        ctx.render("/templates/producto_detalle.html", modelo);
    }

    /** POST /productos/{id}/comentarios → agrega un nuevo comentario */
    public void agregarComentario(Context ctx) {
        int productoId = Integer.parseInt(ctx.pathParam("id"));
        Usuario usuario = ctx.sessionAttribute("usuarioActual");

        if (usuario == null) {
            ctx.status(401).result("Debe iniciar sesión para comentar");
            return;
        }

        String texto = ctx.formParam("texto");
        if (texto == null || texto.isBlank()) {
            ctx.status(400).result("El comentario no puede estar vacío");
            return;
        }

        Comentario comentario = new Comentario(productoId, usuario.getNombre(), texto.trim());
        comentarioGestion.agregarComentario(comentario);

        System.out.println("Comentario agregado por " + usuario.getUsuario() + " en producto " + productoId);

        // Broadcast al canal de comentarios de ese producto
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", "comment_added");
        msg.put("commentId", comentario.getId());
        msg.put("productId", productoId);
        msg.put("autor", comentario.getAutor());
        msg.put("texto", comentario.getTexto());
        msg.put("fecha", comentario.getFecha());

        // Usamos el gson para serializar y enviamos via WebSocket hub
        broadcastNuevoComentario(productoId, comentario);

        ctx.redirect("/productos/" + productoId);
    }

    /** POST /admin/comentarios/{id}/delete → elimina comentario (solo admin) */
    public void eliminarComentario(Context ctx) {
        Usuario usuario = ctx.sessionAttribute("usuarioActual");
        if (usuario == null || !usuario.isConfirmarAdmin()) {
            ctx.status(403).result("Acceso denegado");
            return;
        }

        int comentarioId = Integer.parseInt(ctx.pathParam("id"));
        int productoId = comentarioGestion.eliminarComentario(comentarioId);

        if (productoId == -1) {
            ctx.status(404).result("Comentario no encontrado");
            return;
        }

        // ── WebSocket: notificar en tiempo real a todos los que ven ese producto
        wsGestion.broadcastCommentDeleted(productoId, comentarioId);

        System.out.println("Admin eliminó comentario ID: " + comentarioId + " del producto: " + productoId);

        // Si la petición viene de admin panel, redirigir ahí; si no, al producto
        String referer = ctx.header("Referer");
        if (referer != null && referer.contains("/admin")) {
            ctx.redirect("/admin");
        } else {
            ctx.redirect("/productos/" + productoId);
        }
    }

    // ─── Broadcast privado para nuevo comentario ───────────────────────────────
    private void broadcastNuevoComentario(int productoId, Comentario comentario) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", "comment_added");
        msg.put("commentId", comentario.getId());
        msg.put("productId", productoId);
        msg.put("autor", comentario.getAutor());
        msg.put("texto", comentario.getTexto());
        msg.put("fecha", comentario.getFecha());
        String json = gson.toJson(msg);

        // Accede directamente al canal de comentarios del hub WS
        // (método público en WebSocketGestion)
        wsGestion.broadcastComentarioAgregado(productoId, json);
    }
}