package org.example;

import io.javalin.http.Context;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

/**
 * CORRECCION aplicada:
 *   ProductoGestion en este branch usa obtenerProductoPorId(Long) porque
 *   Producto.id es Long (anotado con @Id @GeneratedValue en JPA).
 *   Se cambio Integer.parseInt -> Long.parseLong en los metodos que
 *   leen el path param "id" y lo pasan a productoGestion.
 */
public class ComentarioControlador {

    private final ComentarioGestion comentarioGestion = ComentarioGestion.getInstance();
    private final WebSocketGestion  wsGestion         = WebSocketGestion.getInstance();
    private final ProductoGestion   productoGestion    = ProductoGestion.getInstance();
    private final Gson gson = new Gson();

    /** GET /productos/{id} - muestra el detalle del producto con sus comentarios */
    public void mostrarDetalleProducto(Context ctx) {

        // CORRECCION: Long.parseLong porque ProductoGestion.obtenerProductoPorId(Long)
        Long id = Long.parseLong(ctx.pathParam("id"));

        Producto producto = productoGestion.obtenerProductoPorId(id).orElse(null);
        if (producto == null) {
            ctx.status(404).result("Producto no encontrado");
            return;
        }

        Map<String, Object> modelo = new HashMap<>();
        modelo.put("producto",    producto);
        modelo.put("comentarios", producto.getComentarios()); // relacion @OneToMany de Hibernate
        modelo.put("usuario",     ctx.sessionAttribute("usuarioActual"));

        ctx.render("/templates/producto_detalle.html", modelo);
    }

    /** POST /productos/{id}/comentarios - agrega un nuevo comentario */
    public void agregarComentario(Context ctx) {

        // CORRECCION: Long.parseLong
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

        // ComentarioGestion usa int internamente
        int productoIdInt = productoId.intValue();
        Comentario comentario = new Comentario(productoIdInt, usuario.getNombre(), texto.trim());
        comentarioGestion.agregarComentario(comentario);

        System.out.println("Comentario agregado por " + usuario.getUsuario()
                + " en producto " + productoId);

        broadcastNuevoComentario(productoIdInt, comentario);
        ctx.redirect("/productos/" + productoId);
    }

    /** POST /admin/comentarios/{id}/delete - elimina comentario (solo admin) */
    public void eliminarComentario(Context ctx) {
        Usuario usuario = ctx.sessionAttribute("usuarioActual");
        if (usuario == null || !usuario.isConfirmarAdmin()) {
            ctx.status(403).result("Acceso denegado");
            return;
        }

        int comentarioId = Integer.parseInt(ctx.pathParam("id"));
        int productoId   = comentarioGestion.eliminarComentario(comentarioId);

        if (productoId == -1) {
            ctx.status(404).result("Comentario no encontrado");
            return;
        }

        wsGestion.broadcastCommentDeleted(productoId, comentarioId);

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