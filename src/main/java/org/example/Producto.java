package org.example;

public class Producto
{
    private static int cont = 0;

    private int id;
    private String nombre;
    private String descripcion;
    private Double precio;
    private int inventario;
    private String imagenBase64;

    public Producto()
    {
        this.id = ++cont;
        this.precio = 0d;
    }

    public Producto( String nombre, Double precio, int inventario )
    {
        this.id = ++cont;
        this.nombre = nombre;
        this.precio = precio;
        this.inventario = inventario;
    }

    public Producto( String nombre, String descripcion, Double precio, int inventario )
    {
        this.id = ++cont;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.inventario = inventario;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public static int getCont() { return cont; }
    public static void setCont(int cont) { Producto.cont = cont; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }

    public int getInventario() { return inventario; }
    public void setInventario(int inventario) { this.inventario = inventario; }

    public String getImagenBase64() { return imagenBase64; }
    public void setImagenBase64(String imagenBase64) { this.imagenBase64 = imagenBase64; }
}