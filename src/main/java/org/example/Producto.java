package org.example;

public class Producto
{
    private static int cont = 0; //Contador para que no hayan IDs repetidos

    private int id;
    private String nombre;
    private Double precio;
    private int inventario;

    public Producto ()
    {
        this.id = ++cont;
        this.precio = 0d; //Intelijj lo hizo automatico, investigué y sirve para dejar el precio en 0 pero como double y el d es para los bits (no acumule tanta memoria)
    }

    //Constructor
    public Producto( String nombre, Double precio, int inventario )
    {
        this.id = ++cont;
        this.nombre = nombre;
        this.precio = precio;
        this.inventario = inventario;
    }

    //Getter and Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public static int getCont() {
        return cont;
    }

    public static void setCont(int cont) {
        Producto.cont = cont;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public int getInventario() {
        return inventario;
    }

    public void setInventario(int inventario) {
        this.inventario = inventario;
    }

}
