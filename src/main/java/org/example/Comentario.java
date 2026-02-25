package org.example;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/*
Puntos 6 y 7:
6- Se permiten comentarios en los productos.
7- Los administradores pueden eliminar comentarios de manera ofensiva
 */

@Entity
public class Comentario
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne( fetch = FetchType.LAZY)
    @JoinColumn( name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column( columnDefinition = "TEXT", nullable = false)
    private String contenido;

    private LocalDateTime fecha = LocalDateTime.now();

    public Comentario() {}

    public Comentario ( Producto producto, Usuario usuario, String contenido )
    {
        this.producto = producto;
        this.usuario = usuario;
        this.contenido = contenido;
        this.fecha = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

}
