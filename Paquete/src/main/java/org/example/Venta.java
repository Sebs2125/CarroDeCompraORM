package org.example;

import java.util.ArrayList;
import java.util.List;

public class Venta {
    private int id;
    private String cliente;
    private List<Producto> productos;
    private double total;

    public Venta(int id, String cliente, List<Producto> productos) {
        this.id = id;
        this.cliente = cliente;
        this.productos = new ArrayList<>(productos);
        this.total = productos.stream().mapToDouble(Producto::getPrecio).sum();
    }

    public int getId() {
        return id;
    }

    public String getCliente() {
        return cliente;
    }

    public List<Producto> getProductos() {
        return productos;
    }

    public double getTotal() {
        return total;
    }

    public int getCantidadItems() {
        return productos.size();
    }
}