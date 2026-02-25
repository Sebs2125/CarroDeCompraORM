package org.example;

import io.javalin.http.Context;

public class ComentarioControlador {
    private ProductoGestion productoGestion = ProductoGestion.getInstance();
    private UsuarioGestion usuarioGestion = UsuarioGestion.getInstance();

    // Agregar comentario
    public void agregarComentario(Context ctx) {
        Usuario usuarioActual = ctx.sessionAttribute("usuarioActual");

        if (usuarioActual == null) {
            ctx.redirect("/login");
            return;
        }

        try {
            Long productoId = Long.parseLong(ctx.formParam("productoId"));
            String contenido = ctx.formParam("contenido");

            if (contenido == null || contenido.trim().isEmpty()) {
                ctx.status(400).result("El comentario no puede estar vacío");
                return;
            }

            Producto producto = productoGestion.obtenerProductoPorId(productoId).orElse(null);
            if (producto == null) {
                ctx.status(404).result("Producto no encontrado");
                return;
            }

            // ✅ REFRESCAR el usuario desde la BD para asegurar que existe
            Usuario usuarioRefrescado = usuarioGestion.buscarUsuario(usuarioActual.getUsuario())
                    .orElse(usuarioActual);

            Comentario comentario = new Comentario(producto, usuarioRefrescado, contenido);
            productoGestion.agregarComentario(comentario);

            ctx.redirect("/producto/" + productoId);

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(400).result("Error al agregar comentario: " + e.getMessage());
        }
    }

    // Eliminar comentario
    public void eliminarComentario(Context ctx) {
        Usuario usuarioActual = ctx.sessionAttribute("usuarioActual");

        if (usuarioActual == null || !usuarioActual.isConfirmarAdmin()) {
            ctx.status(403).result("Acceso denegado");
            return;
        }

        try {
            Long comentarioId = Long.parseLong(ctx.pathParam("id"));
            productoGestion.eliminarComentario(comentarioId);

            String referer = ctx.header("Referer");
            ctx.redirect(referer != null ? referer : "/productos");

        } catch (Exception e) {
            ctx.status(400).result("Error al eliminar comentario: " + e.getMessage());
        }
    }
}