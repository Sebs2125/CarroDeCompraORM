package org.example;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.net.URL;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import java.io.InputStream;
import java.util.*;

import static javax.print.attribute.standard.MediaSizeName.C;

public class ProductoGestion
{
    private static ProductoGestion instance;
    private SessionFactory sessionFactory;

    private ProductoGestion()
    {
        this.sessionFactory = HibernateConsulta.getSessionFactory();
        initializeProductos();
    }

    public static synchronized ProductoGestion getInstance()
    {
        if (instance == null)
        {
            instance = new ProductoGestion();
        }

        return instance;

    }

    private void initializeProductos() //Dando lugar a productos en el software
    {

        Session session = sessionFactory.openSession();

        try {
            Long count = session.createQuery("SELECT COUNT(p) FROM Producto p", Long.class).getSingleResult();
            if (count == 0)
            {
                System.out.println("Inicializando productos por defecto");

                Producto p1 = new Producto("Ram 16GB", "Memoria RAM DDR4 16 GB 3200MHz para alto rendimiento", 1500.0, 20);
                p1.setImagenBase64(cargarImagenDesdeURL("https://via.placeholder.com/400x300/007bff/ffffff?text=RAM+16GB"));
                guardarProducto(session, p1);

                Producto p2 = new Producto("Computadora", "PC de escritorio completa con procesador Intel i5", 5000.0, 15);
                p2.setImagenBase64(cargarImagenDesdeURL("https://via.placeholder.com/400x300/28a745/ffffff?text=Computadora"));
                guardarProducto(session, p2);

                // Producto 3: Laptop
                Producto p3 = new Producto("Laptop", "Laptop ultradelgada 15.6 pulgadas SSD 512GB", 3500.0, 10);
                p3.setImagenBase64(cargarImagenDesdeURL("https://via.placeholder.com/400x300/dc3545/ffffff?text=Laptop"));
                guardarProducto(session, p3);

                // Producto 4: Mouse
                Producto p4 = new Producto("Mouse Logitech", "Mouse inalámbrico ergonómico 2400 DPI", 500.0, 50);
                p4.setImagenBase64(cargarImagenDesdeURL("https://via.placeholder.com/400x300/ffc107/000000?text=Mouse"));
                guardarProducto(session, p4);

                // Producto 5: Teclado
                Producto p5 = new Producto("Teclado Mecanico", "Teclado mecánico RGB switches rojos", 1000.0, 25);
                p5.setImagenBase64(cargarImagenDesdeURL("https://via.placeholder.com/400x300/17a2b8/ffffff?text=Teclado"));
                guardarProducto(session, p5);

                // Producto 6: Monitor
                Producto p6 = new Producto("Monitores", "Monitor 24 pulgadas Full HD 144Hz", 2000.0, 15);
                p6.setImagenBase64(cargarImagenDesdeURL("https://via.placeholder.com/400x300/6c757d/ffffff?text=Monitor"));
                guardarProducto(session, p6);

                System.out.println("6 productos inicializados correctamente");
            }
            else
            {
                System.out.println("Ya existen " + count + " productos en la base de datos");
            }
        } finally {
            session.close();
        }
    }

    private void guardarProducto(Session session, Producto p)
    {
        Transaction tx = session.beginTransaction();
        session.persist(p);
        tx.commit();
    }

    private String cargarImagenDesdeURL( String urlString )
    {
        try {
            URL url = new URL(urlString);
            InputStream is = url.openStream();
            byte[] bytes = is.readAllBytes();
            is.close();
            return Base64.getEncoder().encodeToString(bytes);
        } catch ( Exception e ) {
            System.err.println("Error cargando imagen: " + e.getMessage());
            return "";
        }
    }

    //Tipos de manejo de gestión de productos a la hora de maquinar en el software, sea buscar todos los productos, obtener uno en específico, añadir, deletear, define si quedan de un producto en específico y el modo de reducir el
    // inventario si se llevó un producto al carrito.

    public void addProucto(Producto p)
    {

        if ( p.getImagenBase64() == null || p.getImagenBase64().isEmpty() )
        {
            throw new IllegalArgumentException("La imagen es obligatoria");
        }

       Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();

        try {
            session.persist(p);
            tx.commit();
        } catch ( Exception e )
        {
            tx.rollback();
            throw e;
        } finally {
            session.close();
        }

    }

    public Optional<Producto> obtenerProuctoPorId(Long id)
    {
        Session session = sessionFactory.openSession();

        try {
            Producto p = session.get(Producto.class, id);
            return Optional.ofNullable(p);
        } finally {
            session.close();
        }

    }

    public List<Producto> getListaProductos()
    {
        Session session = sessionFactory.openSession();

        try {
            return session.createQuery("FROM Producto", Producto.class).list();
        } finally {
            session.close();
        }

    }

    public List<Producto> obtenerProductosPaginados( int pagina, int size )
    {
        Session session = sessionFactory.openSession();

        try {
            return session.createQuery("FROM Producto ORDER BY id", Producto.class)
                    .setFirstResult((pagina - 1) * size)
                    .setMaxResults(size)
                    .list();
        } finally {
            session.close();
        }

    }

    public void actualizarProducto(Producto p)
    {
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();

        try {
            session.merge(p);
            tx.commit();
        } catch ( Exception e )
        {
            tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    public void deletearProducto(Long id )
    {
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();

        try {
            Producto p = session.get(Producto.class, id);

            if ( p != null )
            {
                session.remove(p);
            }

            tx.commit();

        } catch ( Exception e )
        {
            tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    public boolean poseeInventario( Long productoId, int cantidad)
    {
        Optional<Producto> producto = obtenerProuctoPorId(productoId);
        return producto.map( p -> p.getInventario() >= cantidad).orElse(false);
    }

    public void reducirInventario(Long productoId, int cantidad)
    {
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();

        try {
            Producto p = session.get(Producto.class, productoId);

            if (p != null)
            {
                p.setInventario(p.getInventario() - cantidad);
                session.merge(p);
            }

            tx.commit();

        } catch ( Exception e )
        {
            tx.rollback();
            throw e;
        } finally {
            session.close();
        }

    }

}
