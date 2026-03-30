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
        String recordar = ctx.formParam("recordar");

        Usuario user = usuarioGestion.autenticarUsuario(usuario, password).orElse(null);

        if ( user != null )
        {
            ctx.sessionAttribute("usuarioActual", user);

            if ( "true".equals(recordar) || "on".equals(recordar) )
            {
                String token = ServicioCookies.generarRecordarUsuarioToken(user.getUsuario());
                ctx.cookie("recordarUsuario", token, 7*24*60*60); //1 semana de datos
            }

            String ip = ctx.ip();
            String userAgent = ctx.userAgent();
            new ManejoDeLoginConsulta().registrarLogin(user.getUsuario(), ip, userAgent);

            ctx.redirect(user.isConfirmarAdmin() ? "/admin" : "/productos");

        }
        else
        {
            ctx.redirect("/login?error=true");
        }

    }

    public void logout(Context ctx)
    {
        ctx.cookie("recordarUsuario", "", 0);
        ctx.req().getSession().invalidate();
        ctx.redirect("/productos");
    }
}