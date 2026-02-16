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

    public void login(Context ctx) // Logica detrás del login, con sus respectivos errores y redirecciones a admin
                                   // y productos y debajo un logout.
    {
        String usuario = ctx.formParam("usuario");
        String password = ctx.formParam("password");

        Usuario user = usuarioGestion.autenticarUsuario(usuario, password).orElse(null);

        if (user != null) {
            ctx.sessionAttribute("usuarioActivo", user);
            System.out.println("Usuario autenticado: " + user.getUsuario() + "-" + user.getNombre() + " (Admin: "
                    + user.isConfirmarAdmin() + ")");

            if (user.isConfirmarAdmin()) {
                ctx.redirect("/admin");
            } else {
                ctx.redirect("/productos");
            }
        } else {
            System.out.println("Intento de login fail para: " + usuario);
            ctx.redirect("/login?error=true");
        }

    }

    public void logout(Context ctx) {
        Usuario usuarioActual = ctx.sessionAttribute("usuarioActual");

        if (usuarioActual != null) {
            System.out.println("Usuario desconecntado: " + usuarioActual.getUsuario());
        }

        ctx.req().getSession().invalidate();
        ctx.redirect("/productos");

    }

}
