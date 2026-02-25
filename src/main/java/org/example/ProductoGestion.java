package org.example;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import java.io.InputStream;
import java.net.URL;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

public class ProductoGestion
{
    private static ProductoGestion instance;
    private EntityManagerFactory emf;

    private ProductoGestion()
    {
        this.emf = Persistence.createEntityManagerFactory("CarritoPU");
        inicializarDatos();
    }

    public static synchronized ProductoGestion getInstance()
    {
        if ( instance == null )
        {
            instance = new ProductoGestion();
        }

        return instance;
    }

    private void inicializarDatos()
    {

        EntityManager em = emf.createEntityManager();

        try
        {
            Long count = em.createQuery("SELECT COUNT(p) FROM Producto p", Long.class).getSingleResult();

            if (count == 0)
            {
                System.out.println("Inicializando productos por defecto...");

                // Producto 1
                Producto p1 = new Producto("RAM 16GB", "Memoria RAM DDR4 16GB 3200MHz para alto rendimiento", 1500.0, 20);
                String img1 = cargarImagenDesdeURL("https://via.placeholder.com/400x300/007bff/ffffff?text=RAM+16GB");
                p1.agregarImagen(new ProductoImagen(p1, img1));
                guardarProducto(em, p1);

                // Producto 2
                Producto p2 = new Producto("Computadora", "PC de escritorio completa con procesador Intel i5", 5000.0, 15);
                String img2 = cargarImagenDesdeURL("https://via.placeholder.com/400x300/28a745/ffffff?text=Computadora");
                p2.agregarImagen(new ProductoImagen(p2, img2));
                guardarProducto(em, p2);

                // Producto 3
                Producto p3 = new Producto("Laptop", "Laptop ultradelgada 15.6 pulgadas SSD 512GB", 3500.0, 10);
                String img3 = cargarImagenDesdeURL("https://via.placeholder.com/400x300/dc3545/ffffff?text=Laptop");
                p3.agregarImagen(new ProductoImagen(p3, img3));
                guardarProducto(em, p3);

                // Producto 4
                Producto p4 = new Producto("Mouse Logitech", "Mouse inalámbrico ergonómico 2400 DPI", 500.0, 50);
                String img4 = cargarImagenDesdeURL("https://via.placeholder.com/400x300/ffc107/000000?text=Mouse");
                p4.agregarImagen(new ProductoImagen(p4, img4));
                guardarProducto(em, p4);

                // Producto 5
                Producto p5 = new Producto("Teclado Mecanico", "Teclado mecánico RGB switches rojos", 1000.0, 25);
                String img5 = cargarImagenDesdeURL("https://via.placeholder.com/400x300/17a2b8/ffffff?text=Teclado");
                p5.agregarImagen(new ProductoImagen(p5, img5));
                guardarProducto(em, p5);

                // Producto 6
                Producto p6 = new Producto("Monitores", "Monitor 24 pulgadas Full HD 144Hz", 2000.0, 15);
                String img6 = cargarImagenDesdeURL("https://via.placeholder.com/400x300/6c757d/ffffff?text=Monitor");
                p6.agregarImagen(new ProductoImagen(p6, img6));
                guardarProducto(em, p6);

                System.out.println("6 productos inicializados correctamente");
            } else
            {
                System.out.println("Ya existen " + count + " productos en la BD");
            }
        } finally
        {
            em.close();
        }
    }

    private void guardarProducto(EntityManager em, Producto producto)
    {
        em.getTransaction().begin();
        em.persist(producto);
        em.getTransaction().commit();
    }

    private String cargarImagenDesdeURL(String urlString)
    {
        try
        {
            URL url = new URL(urlString);
            InputStream is = url.openStream();
            byte[] bytes = is.readAllBytes();
            is.close();
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e)
        {
            System.err.println("Error cargando imagen: " + e.getMessage());
            return "";
        }
    }


    public void addProducto(Producto producto)
    {
        if (producto.getImagenes() == null || producto.getImagenes().isEmpty())
        {
            throw new IllegalArgumentException("La imagen es obligatoria para el producto");
        }

        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();
            em.persist(producto);
            em.getTransaction().commit();
        } catch (Exception e)
        {
            em.getTransaction().rollback();
            throw e;
        } finally
        {
            em.close();
        }
    }

    // READ por ID con imágenes y comentarios
    public Optional<Producto> obtenerProductoPorId(Long id)
    {

        EntityManager em = emf.createEntityManager();

        try {

            Producto p = em.createQuery(
                            "SELECT DISTINCT p FROM Producto p " +
                                    "LEFT JOIN FETCH p.imagenes " +
                                    "LEFT JOIN FETCH p.comentarios c " +
                                    "LEFT JOIN FETCH c.usuario " +
                                    "WHERE p.id = :id", Producto.class)
                    .setParameter("id", id)
                    .getSingleResult();

            return Optional.ofNullable(p);

        } catch (Exception e)
        {
            // Si no encuentra con JOIN FETCH, intentar sin
            try
            {
                Producto p = em.find(Producto.class, id);

                if ( p != null )
                {
                    p.getImagenes().size();
                    p.getComentarios().forEach(c -> {
                        if (c.getUsuario() != null) {
                            c.getUsuario().getNombre(); // Forzar carga
                        }
                    });
                }

                return Optional.ofNullable(p);

            } catch (Exception ex)
            {
                return Optional.empty();
            }
        } finally
        {
            em.close();
        }
    }


    public List<Producto> getListaProductos()
    {
        EntityManager em = emf.createEntityManager();
        try
        {
            return em.createQuery("SELECT p FROM Producto p", Producto.class).getResultList();

        } finally
        {
            em.close();
        }
    }

    // READ con paginación (Punto 8)
    public List<Producto> obtenerProductosPaginados(int pagina, int tamano)
    {
        EntityManager em = emf.createEntityManager();

        try
        {
            return em.createQuery("SELECT p FROM Producto p ORDER BY p.id", Producto.class)
                    .setFirstResult((pagina - 1) * tamano)
                    .setMaxResults(tamano)
                    .getResultList();
        } finally
        {
            em.close();
        }
    }

    // Contar total para paginación
    public long contarProductos()
    {
        EntityManager em = emf.createEntityManager();

        try
        {
            return em.createQuery("SELECT COUNT(p) FROM Producto p", Long.class).getSingleResult();
        } finally
        {
            em.close();
        }
    }

    public void actualizarProducto(Producto producto)
    {
        EntityManager em = emf.createEntityManager();

        try
        {
            em.getTransaction().begin();
            em.merge(producto);
            em.getTransaction().commit();
        } catch (Exception e)
        {
            em.getTransaction().rollback();
            throw e;
        } finally
        {
            em.close();
        }
    }

    // DELETE
    public void deletearProducto(Long id)
    {
        EntityManager em = emf.createEntityManager();

        try
        {
            em.getTransaction().begin();
            Producto p = em.find(Producto.class, id);

            if (p != null)
            {
                em.remove(p);
            }

            em.getTransaction().commit();

        } catch (Exception e)
        {
            em.getTransaction().rollback();
            throw e;
        } finally
        {
            em.close();
        }
    }

    // Métodos de negocio
    public boolean poseeInventario(Long productoId, int cantidad)
    {
        Optional<Producto> producto = obtenerProductoPorId(productoId);
        return producto.map(p -> p.getInventario() >= cantidad).orElse(false);
    }

    public void reducirInventario(Long productoId, int cantidad)
    {

        EntityManager em = emf.createEntityManager();

        try
        {
            em.getTransaction().begin();
            Producto p = em.find(Producto.class, productoId);

            if (p != null)
            {
                p.setInventario(p.getInventario() - cantidad);
                em.merge(p);
            }

            em.getTransaction().commit();

        } catch (Exception e)
        {
            em.getTransaction().rollback();
            throw e;
        } finally
        {
            em.close();
        }
    }

    // Agregar comentario
    public void agregarComentario(Comentario comentario)
    {

        EntityManager em = emf.createEntityManager();

        try
        {
            em.getTransaction().begin();

            Producto productoManaged = em.find(Producto.class, comentario.getProducto().getId());
            Usuario usuarioManaged = em.find(Usuario.class, comentario.getUsuario().getId());

            comentario.setProducto(productoManaged);
            comentario.setUsuario(usuarioManaged);

            em.persist(comentario);
            em.getTransaction().commit();
        } catch (Exception e)
        {
            em.getTransaction().rollback();
            throw e;
        } finally
        {
            em.close();
        }
    }

    // Eliminar comentario
    public void eliminarComentario(Long comentarioId)
    {
        EntityManager em = emf.createEntityManager();

        try
        {
            em.getTransaction().begin();
            Comentario c = em.find(Comentario.class, comentarioId);

            if (c != null)
            {
                em.remove(c);
            }
            em.getTransaction().commit();
        } catch (Exception e)
        {
            em.getTransaction().rollback();
            throw e;
        } finally
        {
            em.close();
        }
    }

    public void shutdown()
    {
        if (emf != null && emf.isOpen())
        {
            emf.close();
        }
    }
}