package org.example;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.Optional;

public class UsuarioConsulta
{
    private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("Carrito");

    //Crear el usuario
    public void crear(Usuario usuario)
    {
        Session session = HibernateConsulta.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        session.persist(usuario);
        tx.commit();
        session.close();
    }

    // Buscar usuario por username (para login)
    public Optional<Usuario> buscarPorUsername(String username)
    {
        Session session = HibernateConsulta.getSessionFactory().openSession();
        Usuario usuario = session.createQuery("FROM Usuario u WHERE u.usuario = :username", Usuario.class)
                .setParameter("username", username)
                .uniqueResult();
        session.close();

        return Optional.ofNullable(usuario);

    }

    // Buscar usuario por id
    public Usuario buscarPorId(Long id)
    {
        Session session = HibernateConsulta.getSessionFactory().openSession();
        Usuario usuario = session.get(Usuario.class, id);
        session.close();

        return usuario;

    }

    // Actualizar usuario (ejemplo: cambiar contraseña)
    public void actualizar(Usuario usuario)
    {
        Session session = HibernateConsulta.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        session.merge(usuario);
        tx.commit();
        session.close();
    }

    // Eliminar usuario
    public void eliminar(Long id)
    {
        Session session = HibernateConsulta.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        Usuario usuario = session.get(Usuario.class, id);

        if (usuario != null)
        {
            session.remove(usuario);
        }

        tx.commit();
        session.close();
    }

}
