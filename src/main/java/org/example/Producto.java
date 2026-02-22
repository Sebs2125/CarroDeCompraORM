package org.example;

import jakarta.persistence.*;

import java.util.List;

/*
Se manejan varios puntos en esta clase, puntos:
3- Los modelos se deben relacionar con una entidad
5- CRUD de producto con imagenes en Base64
6- Vista y logica de producto referido a descripciones, imagenes y comentarios
8- Paginacion de productos (gestion desde DAO)
 */

@Entity
public class Producto
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String descripcion;
    private Double precio;
    private int inventario;

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductoImagen> imagenes;

    @OneToMany( mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comentario> comentarios;

    public Producto ()
    {
    }

    //Constructor
    public Producto( String nombre, String descripcion, Double precio, int inventario )
    {
        this.descripcion = descripcion;
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
