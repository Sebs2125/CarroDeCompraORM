package org.example;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class ComentarioConsulta
{
    private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("Carrito");

    //Crear el comentario
    public void crear(Comentario comentario)
    {
        Session session = HibernateConsulta.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        session.persist(comentario);
        tx.commit();
        session.close();
    }

    // Buscar comentario por id
    public Comentario buscarPorId(Long id)
    {
        Session session = HibernateConsulta.getSessionFactory().openSession();
        Comentario comentario = session.get(Comentario.class, id);
        session.close();

        return comentario;
    }

    // Obtener comentarios por producto
    public List<Comentario> comentariosPorProducto(Long productoId)
    {
        Session session = HibernateConsulta.getSessionFactory().openSession();
        List<Comentario> lista = session.createQuery("FROM Comentario c WHERE c.producto.id = :productoId", Comentario.class)
                .setParameter("productoId", productoId)
                .getResultList();
        session.close();

        return lista;

    }

    // Actualizar comentario
    public void actualizar(Comentario comentario)
    {
        Session session = HibernateConsulta.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        session.merge(comentario);
        tx.commit();
        session.close();
    }

    // Eliminar comentario (admin)
    public void eliminar(Long id)
    {
        Session session = HibernateConsulta.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        Comentario comentario = session.get(Comentario.class, id);

        if (comentario != null)
        {
            session.remove(comentario);
        }

        tx.commit();
        session.close();
    }

}
