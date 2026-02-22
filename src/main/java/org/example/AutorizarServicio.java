package org.example;

/*
Punto 4, 5, 6, 7, 9
Servicio de autenticación y gestión de usuarios
 */

import java.util.Optional;

import static java.util.Locale.filter;

public class AutorizarServicio
{
    private final UsuarioConsulta usuarioConsulta = new UsuarioConsulta();
    private final ManejoDeLoginConsulta manejoDeLoginConsulta = new ManejoDeLoginConsulta();

    public Optional<Usuario> login ( String username, String password, String ip, String agent, boolean recordar )
    {
        Optional<Usuario> optionalUsuario = usuarioConsulta.buscarPorUsername(username)
                .filter(u -> u.getPassword().equals(password));

        if ( optionalUsuario.isPresent() )
        {
            manejoDeLoginConsulta.registrarLogin( username, ip, agent);
        }

        return optionalUsuario;

    }

    //Generar cookie de recordar usuario
    public String generarCookieRecordar( String usuario )
    {
        return ServicioCookies.validaryExtraerUsuario( usuario );
    }

    //Validar cookie de Recordar usuario
    public String validarCookiesRecordar( String token )
    {
        return ServicioCookies.validaryExtraerUsuario( token );
    }

    //Registrar nuevo usuario
    public void registrarUsuario( String username, String password, boolean esAdmin )
    {
        Usuario usuario = new Usuario( username, password, esAdmin );
        usuarioConsulta.crear( usuario );
    }

    //Actualizar usuario
    public void actualizarUsuario( Usuario usuario )
    {
        usuarioConsulta.actualizar( usuario );
    }

    //Eliminar usuario
    public void eliminarUsuario( Long id )
    {
        usuarioConsulta.eliminar( id );
    }

    //Buscar usuario por username
    public Optional<Usuario> buscarPorUsername( String username )
    {
        return usuarioConsulta.buscarPorUsername( username );
    }

    //Buscar usuario por id
    public Usuario buscarPorId(Long id )
    {
        return usuarioConsulta.buscarPorId( id );
    }


}
