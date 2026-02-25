package org.example;

import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.spi.ToolProvider.findFirst;

public class UsuarioGestion
{
    private static UsuarioGestion instance;

    private UsuarioGestion()
    {
        inicializarDatos();
    }

    public static synchronized UsuarioGestion getInstance() //Instancia para gestionar a los usuarios registrados
    {
        if ( instance == null )
        {
            instance = new UsuarioGestion();
        }

       return instance;

    }

    private void inicializarDatos()
    {
        Session session = null;
        try {

            session = HibernateConsulta.getSessionFactory().openSession();

            Long count = session.createQuery("SELECT COUNT(u) FROM Usuario u", Long.class).uniqueResult();

            if (count == 0)
            {
                System.out.println("Inicializando usuarios por defecto...");

                Transaction tx = session.beginTransaction();

                try {
                    Usuario admin = new Usuario("admin", "Administrador", "admin", true);
                    Usuario u1 = new Usuario("usuario1", "Sebastian Almanzar", "usuario1", false);
                    Usuario u2 = new Usuario("usuario2", "Esmil Echavarria", "usuario2", false);

                    session.persist(admin);
                    session.persist(u1);
                    session.persist(u2);

                    tx.commit();
                    System.out.println("Usuarios creados exitosamente");
                } catch (Exception e) {
                    tx.rollback();
                    System.err.println("Error creando usuarios: " +e.getMessage());
                    e.printStackTrace();
                }

            } else
            {
                System.out.println("Ya existen " + count + " usuarios en la BD");
            }
        } catch (Exception e)
        {
            System.err.println("Error al inicializar usuarios por defecto: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if ( session != null && session.isOpen() )
            {
                session.close();
            }
        }
    }

    public Optional<Usuario> autenticarUsuario(String usuario, String password) //Verifica que el usuario existe o no
    {
        Session session = null;

        try {

            session = HibernateConsulta.getSessionFactory().openSession();

            System.out.println("Buscando usuario: '" + usuario + "' con password: '" + password + "'");

            Usuario user = session.createQuery("FROM Usuario u WHERE u.usuario = :username AND u.password = :pass", Usuario.class)
                    .setParameter("username", usuario)
                    .setParameter("pass", password)
                    .uniqueResult();

            System.out.println("Resultado: " + (user != null ? "ENCONTRADO" : "NO ENCONTRADO"));

            return Optional.ofNullable(user);

        } catch ( Exception e ) {
            System.err.println("Error en autenticarUsuario: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        } finally {
            if ( session != null && session.isOpen() )
            {
                session.close();
            }
        }
    }

    public Optional<Usuario> buscarPorUsernameSimple(String username) {
        Session session = null;
        try {
            session = HibernateConsulta.getSessionFactory().openSession();
            List<Usuario> usuarios = session.createQuery(
                            "FROM Usuario u WHERE u.usuario = :username",
                            Usuario.class)
                    .setParameter("username", username)
                    .getResultList();

            System.out.println("Búsqueda simple de '" + username + "': " + usuarios.size() + " resultados");
            for (Usuario u : usuarios) {
                System.out.println("  - " + u.getUsuario() + " / " + u.getPassword());
            }

            return usuarios.isEmpty() ? Optional.empty() : Optional.of(usuarios.get(0));
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    public Optional<Usuario> buscarUsuario(String usuario)
    {
        Session session = HibernateConsulta.getSessionFactory().openSession();

        try {
            Usuario user = session.createQuery(
                    "FROM Usuario u WHERE u.usuario = :username", Usuario.class)
                    .setParameter("username", usuario)
                    .uniqueResult();
            return Optional.ofNullable(user);
        } finally {
            session.close();
        }

    }

    public List<Usuario> todosLosUsuarios()
    {
        Session session = HibernateConsulta.getSessionFactory().openSession();

        try {
            return session.createQuery("FROM Usuario u", Usuario.class).list();
        } finally {
            session.close();
        }

    }

    public void addUsuario( Usuario usuario)
    {
        Session session = HibernateConsulta.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();

        try {
            session.persist(usuario);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw e;
        } finally {
            session.close();
        }

    }

}
