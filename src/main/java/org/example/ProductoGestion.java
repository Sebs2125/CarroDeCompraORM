package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductoGestion
{
    private static ProductoGestion instance;
    private List<Producto> productos;

    private ProductoGestion()
    {
        productos = new ArrayList<>();
        initializeProductos();
    }

    private void initializeProductos() //Dando lugar a productos en el software
    {
        productos.add(new Producto("RAM 16GB", 1500.0, 20 ) );
        productos.add(new Producto("Computadora", 5000.0, 15 ) );
        productos.add(new Producto("Laptop", 3500.0, 10 ) );
        productos.add(new Producto("Mouse Logitech", 500.0, 50 ) );
        productos.add(new Producto("Teclado Mecanico", 1000.0, 25 ) );
        productos.add(new Producto("Monitores", 2000.0, 15 ) );
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

    public Optional<Producto> obtenerProductoPorId(int id)
    {
        return productos.stream()
                .filter(producto -> producto.getId() == id)
                .findFirst();
    }

    public void addProducto(Producto producto)
    {
        productos.add(producto);
        System.out.println("Producto agregado: " + producto );
    }

    public void actualizarProducto(Producto producto)
    {
        Optional<Producto> productoYaExiste = obtenerProductoPorId(producto.getId());
        productoYaExiste.ifPresent( p -> {
            p.setNombre(producto.getNombre());
            p.setPrecio(producto.getPrecio());
            p.setInventario(producto.getInventario());
            System.out.println("Producto actualizado: " + p);
        });
    }

    public void deletearProducto( int id )
    {
        productos.removeIf( p ->  p.getId() == id );
        System.out.println("Producto eliminado con ID: " + id );
    }

    public boolean poseeInventario(int productoId, int cantidad ) //Se verifica si de un producto se encuentra en el inventario o no.
    {
        Optional<Producto> producto = obtenerProductoPorId(productoId);
        return producto.map(p -> p.getInventario() >= cantidad).orElse( false );
    }

    public void reducirInventario(int productoId, int cantidad ) //Si se mete un producto en el Carrito de compra, debe reducirse el inventario.
    {
        Optional<Producto> producto = obtenerProductoPorId(productoId);
        producto.ifPresent( p -> p.setInventario( p.getInventario() - cantidad ) );
    }

}
