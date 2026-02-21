package org.example;

import java.util.ArrayList;
import java.util.List;

public class CarritoDeCompra
{

    private static long contCarritoDeCompra = 0;
    private long id;
    private List<Producto> listaProductos;

    public CarritoDeCompra()
    {
        this.id = ++contCarritoDeCompra;
        this.listaProductos = new ArrayList<Producto>();
    }

    public static long getContCarritoDeCompra() {
        return contCarritoDeCompra;
    }

    public static void setContCarritoDeCompra(long contCarritoDeCompra) {
        CarritoDeCompra.contCarritoDeCompra = contCarritoDeCompra;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<Producto> getListaProductos() {
        return listaProductos;
    }

    public void setListaProductos(List<Producto> listaProductos) {
        this.listaProductos = listaProductos;
    }

    public void agregarProducto(Producto producto)
    {
        this.listaProductos.add(producto);
    }

    public int getCantidadProductos()
    {
        return this.listaProductos.size();
    }

    public Double getTotal()
    {
        double total = 0.0;

        for ( Producto producto : this.listaProductos )
        {
            total += producto.getPrecio();
        }

        return total;

    }

    public void limpiar()
    {
        this.listaProductos.clear();
    }

    @Override
    public String toString()
    {
        return "CarroDeCompra{" + "id=" + id + ", cantidadDeProductos=" + getCantidadProductos() + ", total=" + getTotal() + '}';
    }

}
