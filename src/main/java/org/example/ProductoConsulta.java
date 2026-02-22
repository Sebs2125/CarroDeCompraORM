package org.example;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.hibernate.Session;
import org.hibernate.Transaction;


import java.util.List;

public class ProductoConsulta
{
    private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("Carrito");

    //Crear el producto
    public void crear (Producto producto)
    {
        Session session = HibernateConsulta.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        session.persist(producto);
        tx.commit();
        session.close();
    }

    // Buscar producto por id (Read)
    public Producto buscarPorId(Long id)
    {
        Session session = HibernateConsulta.getSessionFactory().openSession();
        Producto producto = session.get(Producto.class, id);
        session.close();
        return producto;
    }

    // Actualizar producto
    public void actualizar(Producto producto)
    {
        Session session = HibernateConsulta.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        session.merge(producto);
        tx.commit();
        session.close();
    }

    // Eliminar producto
    public void eliminar(Long id)
    {
        Session session = HibernateConsulta.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        Producto producto = session.get(Producto.class, id);

        if (producto != null)
        {
            session.remove(producto);
        }

        tx.commit();
        session.close();

    }

    // Obtener todos los productos
    public List<Producto> obtenerTodos()
    {
        Session session = HibernateConsulta.getSessionFactory().openSession();
        List<Producto> lista = session.createQuery("FROM Producto", Producto.class).getResultList();
        session.close();

        return lista;

    }

    // Paginación: obtener productos por página (10 por página)
    public List<Producto> paginated(int page, int size)
    {
        Session session = HibernateConsulta.getSessionFactory().openSession();
        List<Producto> result = session.createQuery("FROM Producto", Producto.class)
                .setFirstResult((page - 1) * size)
                .setMaxResults(size)
                .getResultList();
        session.close();

        return result;

    }

}
