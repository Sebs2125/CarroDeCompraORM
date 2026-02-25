package org.example;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Producto
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private Double precio;
    private int inventario;

    @OneToMany (mappedBy = "producto", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<ProductoImagen> imagenes = new ArrayList<>();

    @OneToMany (mappedBy = "producto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comentario> comentarios = new ArrayList<>();

    public Producto()
    {
        this.precio = 0d;
    }

    public Producto( String nombre, Double precio, int inventario )
    {
        this.nombre = nombre;
        this.precio = precio;
        this.inventario = inventario;
    }

    public Producto( String nombre, String descripcion, Double precio, int inventario )
    {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.inventario = inventario;
    }

    public void agregarImagen( ProductoImagen imagen )
    {
        imagenes.add(imagen);
        imagen.setProducto(this);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }

    public int getInventario() { return inventario; }
    public void setInventario(int inventario) { this.inventario = inventario; }

    public List<ProductoImagen> getImagenes() {
        return imagenes;
    }

    public void setImagenes(List<ProductoImagen> imagenes) {
        this.imagenes = imagenes;
    }

    public List<Comentario> getComentarios() {
        return comentarios;
    }

    public void setComentarios(List<Comentario> comentarios) {
        this.comentarios = comentarios;
    }

    public String getImagenBase64()
    {
        return imagenes.isEmpty() ? null : imagenes.get(0).getBase64();
    }

}