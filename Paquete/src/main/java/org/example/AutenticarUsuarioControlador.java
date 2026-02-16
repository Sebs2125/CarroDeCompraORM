package org.example;

import java.util.HashMap;
import java.util.Map;
import io.javalin.http.Context;

public class AutenticarUsuarioControlador {
    private UsuarioGestion usuarioGestion = UsuarioGestion.getInstance();

    public void mostrarPaginaLogin(Context ctx) {
        Map<String, Object> modelo = new HashMap<>();
        String error = ctx.queryParam("error");

        if (error != null) {
            modelo.put("error", "Usuario o password incorrecto");
        }

        ctx.render("/templates/login.html", modelo);
    }

    public void login(Context ctx) {
        String usuario = ctx.formParam("usuario");
        String password = ctx.formParam("password");

        Usuario user = usuarioGestion.autenticarUsuario(usuario, password).orElse(null);

        if (user != null) {
            // CORREGIDO: Usamos "usuarioActual" para que coincida con ProductoControlador
            ctx.sessionAttribute("usuarioActual", user);

            System.out
                    .println("Usuario autenticado: " + user.getUsuario() + " (Admin: " + user.isConfirmarAdmin() + ")");

            if (user.isConfirmarAdmin()) {
                ctx.redirect("/admin");
            } else {
                ctx.redirect("/productos");
            }
        } else {
            System.out.println("Intento de login fallido para: " + usuario);
            ctx.redirect("/login?error=true");
        }
    }

    public void logout(Context ctx) {
        // CORREGIDO: Usamos "usuarioActual"
        Usuario usuarioActual = ctx.sessionAttribute("usuarioActual");

        if (usuarioActual != null) {
            System.out.println("Usuario desconectado: " + usuarioActual.getUsuario());
        }

        ctx.req().getSession().invalidate();
        ctx.redirect("/productos");
    }
}