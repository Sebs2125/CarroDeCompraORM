package org.example;

import jakarta.persistence.*;
/*
Punto 5:
5- En el crud se permiten imagenes que se almacenan en Base64.
 */

@Entity
public class ProductoImagen
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column( columnDefinition = "LONGTEXT" )
    private String base64;

    public ProductoImagen () {}

    public ProductoImagen ( Producto producto, String base64 )
    {
        this.producto = producto;
        this.base64 = base64;
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

    public String getBase64() {
        return base64;
    }

    public void setBase64(String base64) {
        this.base64 = base64;
    }

}
