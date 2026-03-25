package org.example;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "comentario")
public class Comentario
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relacion con Producto — requerida por ProductoGestion.agregarComentario()
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id")
    private Producto producto;

    // Relacion con Usuario — requerida por ProductoGestion.agregarComentario()
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(columnDefinition = "TEXT")
    private String texto;

    private String fecha;

    // ── Constructores

    public Comentario() {}

    /** Constructor JPA con relaciones a entidades */
    public Comentario(Producto producto, Usuario usuario, String texto) {
        this.producto = producto;
        this.usuario  = usuario;
        this.texto    = texto;
        this.fecha    = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    // ── Getters y Setters

    public Long getId()            { return id; }
    public void setId(Long id)     { this.id = id; }

    public Producto getProducto()              { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }

    public Usuario getUsuario()            { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public String getTexto()           { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    public String getFecha()           { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    // ── Metodos de conveniencia

    public String getAutor() {
        return usuario != null ? usuario.getNombre() : "Anonimo";
    }

    public int getProductoId() {
        return producto != null ? producto.getId().intValue() : 0;
    }
}