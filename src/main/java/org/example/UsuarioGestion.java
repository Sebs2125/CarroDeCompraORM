package org.example;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.List;
import java.util.Optional;

public class UsuarioGestion
{
    private static UsuarioGestion instance;
    private EntityManagerFactory emf;

    private UsuarioGestion()
    {
        this.emf = Persistence.createEntityManagerFactory("CarritoPU");
        inicializarDatos();
    }

    public static synchronized UsuarioGestion getInstance()
    {
        if ( instance == null )
        {
            instance = new UsuarioGestion();
        }
        return instance;
    }

    private void inicializarDatos()
    {

        EntityManager em = emf.createEntityManager();

        try
        {
            Long count = em.createQuery("SELECT COUNT(u) FROM Usuario u", Long.class).getSingleResult();

            if (count == 0)
            {
                System.out.println("Creando usuarios por defecto...");

                em.getTransaction().begin();

                Usuario admin = new Usuario("admin", "Administrador", "admin", true);
                Usuario u1 = new Usuario("usuario1", "Sebastian Almanzar", "usuario1", false);
                Usuario u2 = new Usuario("usuario2", "Esmil Echavarria", "usuario2", false);

                em.persist(admin);
                em.persist(u1);
                em.persist(u2);

                em.getTransaction().commit();
                System.out.println("3 usuarios creados");
            }
        } catch (Exception e)
        {
            System.err.println("Error creando usuarios: " + e.getMessage());
            e.printStackTrace();
        } finally
        {
            em.close();
        }
    }

    public Optional<Usuario> autenticarUsuario(String usuario, String password)
    {

        EntityManager em = emf.createEntityManager();

        try
        {
            List<Usuario> result = em.createQuery(
                            "SELECT u FROM Usuario u WHERE u.usuario = :username AND u.password = :pass",
                            Usuario.class)
                    .setParameter("username", usuario)
                    .setParameter("pass", password)
                    .getResultList();

            return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));

        } finally
        {
            em.close();
        }
    }

    public Optional<Usuario> buscarUsuario(String usuario)
    {

        EntityManager em = emf.createEntityManager();

        try
        {
            List<Usuario> result = em.createQuery(
                            "SELECT u FROM Usuario u WHERE u.usuario = :username",
                            Usuario.class)
                    .setParameter("username", usuario)
                    .getResultList();

            return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));

        } finally
        {
            em.close();
        }
    }

    public List<Usuario> todosLosUsuarios()
    {
        EntityManager em = emf.createEntityManager();

        try
        {
            return em.createQuery("SELECT u FROM Usuario u", Usuario.class).getResultList();

        } finally
        {
            em.close();
        }
    }
}