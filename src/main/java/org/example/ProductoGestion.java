package org.example;

import java.io.InputStream;
import java.util.*;

import static javax.print.attribute.standard.MediaSizeName.C;

public class ProductoGestion
{
    private static ProductoGestion instance;
    private List<Producto> productos;
    private long nextId = 1;

    private ProductoGestion()
    {
        productos = new ArrayList<>();
        initializeProductos();
    }

    private String cargarImagenDesdeResources( String nombreArchivo )
    {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("/images/" + nombreArchivo);

            if ( is == null )
            {
                System.out.println("No se encontro ninguna imagen: " +nombreArchivo);
                return null;
            }

            byte[] bytes = is.readAllBytes();
            is.close();
            return Base64.getEncoder().encodeToString(bytes);

        } catch ( Exception e ) {
            System.out.println("Error cargando " + nombreArchivo + ": " + e.getMessage());
            return null;
        }
    }

    private void initializeProductos() //Dando lugar a productos en el software
    {
        Producto p1 = new Producto("RAM 16GB", 1500.0, 20);
        p1.setId(nextId++);
        p1.setImagenBase64(cargarImagenDesdeResources("ram16gb.jpg"));
        productos.add(p1);


        Producto p2 = new Producto("Computadora", 5000.00, 15);
        p2.setId(nextId++);
        p2.setImagenBase64(cargarImagenDesdeResources("pcGaming.jpg"));
        productos.add(p2);


        Producto p3 = new Producto("Laptop", 3500.0, 10);
        p3.setId(nextId++);
        p3.setImagenBase64(cargarImagenDesdeResources("laptopGaming.jpg"));
        productos.add(p3);

        Producto p4 = new Producto("Mouse Logitech", 500.0, 50 );
        p4.setId(nextId++);
        p4.setImagenBase64(cargarImagenDesdeResources("mouseLogitech.jpg"));
        productos.add(p4);


        Producto p5 = new Producto("Teclado Mecanico", 1000.0, 25);
        p5.setId(nextId++);
        p5.setImagenBase64(cargarImagenDesdeResources("tecladoMecanico.jpg"));
        productos.add(p5);


        Producto p6 = new Producto("Monitores", 2000.0, 15);
        p6.setId(nextId++);
        p6.setImagenBase64(cargarImagenDesdeResources("MonitorGaming.jpg"));
        productos.add(p6);


    }

    public static ProductoGestion getInstance()
    {
        if ( instance == null )
        {
            instance = new ProductoGestion();
        }

        return instance;

    }

    //Tipos de manejo de gestión de productos a la hora de maquinar en el software, sea buscar todos los productos, obtener uno en específico, añadir, deletear, define si quedan de un producto en específico y el modo de reducir el
    // inventario si se llevó un producto al carrito.

    public List<Producto> getListaProductos()
    {
        return new ArrayList<>(productos);
    }

    public Optional<Producto> obtenerProductoPorId(Long id)
    {
        return productos.stream()
                .filter(producto -> Objects.equals(producto.getId(), id))
                .findFirst();
    }

    public void addProducto(Producto producto)
    {
        producto.setId(nextId++);
        productos.add(producto);
        System.out.println("Producto agregado: " + producto );
    }

    public void actualizarProducto(Producto producto)
    {
        obtenerProductoPorId(producto.getId()).ifPresent(p -> {
            p.setNombre(producto.getNombre());
            p.setDescripcion(producto.getDescripcion());
            p.setPrecio(producto.getPrecio());
            p.setInventario(producto.getInventario());
            if (producto.getImagenBase64() != null &&  !producto.getImagenBase64().isEmpty()) {
                p.setImagenBase64(producto.getImagenBase64());
            }
            System.out.println("Producto actualizado: " + p.getNombre());
        });
    }

    public void deletearProducto( Long id )
    {
        productos.removeIf( p ->  p.getId().equals(id) );
        System.out.println("Producto eliminado con ID: " + id );
    }

    public boolean poseeInventario(Long productoId, int cantidad ) //Se verifica si de un producto se encuentra en el inventario o no.
    {
        Optional<Producto> producto = obtenerProductoPorId(productoId);
        return producto.map(p -> p.getInventario() >= cantidad).orElse( false );
    }

    public void reducirInventario(Long productoId, int cantidad ) //Si se mete un producto en el Carrito de compra, debe reducirse el inventario.
    {
        Optional<Producto> producto = obtenerProductoPorId(productoId);
        producto.ifPresent( p -> {
            int nuevoInventario = p.getInventario() - cantidad;
            p.setInventario(nuevoInventario);
            System.out.println("Inventario reducido - Producto: " +p.getNombre() + ", Nuevo stock: " + nuevoInventario);
        });

    }

}
