package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.spi.ToolProvider.findFirst;

public class UsuarioGestion
{
    private static UsuarioGestion instance;
    private List<Usuario> usuarios;

    private UsuarioGestion()
    {
        usuarios = new ArrayList<>();
        usuarios.add(new Usuario("admin", "Administrador", "admin", true) ); //Creacion de usuarios para entrar a su Carrito de Compras
        usuarios.add(new Usuario("usuario", "Sebastian Almanzar", "usuario1", false) );
        usuarios.add(new Usuario("usuario2", "Esmil Echavarria", "usuario2", false) );
    }

    public static UsuarioGestion getInstance() //Instancia para gestionar a los usuarios registrados
    {
        if ( instance == null )
        {
            instance = new UsuarioGestion();
        }

       return instance;

    }

    public Optional<Usuario> autenticarUsuario(String usuario, String password) //Verifica que el usuario existe o no
    {
        return usuarios.stream()
                .filter( u -> u.getUsuario().equals(usuario) && u.getPassword().equals(password ))
                .findFirst();
    }

    public void addUsuario(Usuario usuario)
    {
        usuarios.add(usuario);
    }

    public List<Usuario> todosLosUsuarios()
    {
        return new ArrayList<>(usuarios);
    }

    public Optional<Usuario> buscarUsuario(String usuario)
    {
        return usuarios.stream()
                .filter( u -> u.getUsuario().equals(usuario) )
                .findFirst();
    }

}
