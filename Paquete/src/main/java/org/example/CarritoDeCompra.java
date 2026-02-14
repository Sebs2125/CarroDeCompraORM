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

}
