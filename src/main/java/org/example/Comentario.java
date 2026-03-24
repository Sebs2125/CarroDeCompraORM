package org.example;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Comentario {

    private static int cont = 0;

    private int id;
    private int productoId;
    private String autor;
    private String texto;
    private String fecha;

    public Comentario() {}

    public Comentario(int productoId, String autor, String texto) {
        this.id = ++cont;
        this.productoId = productoId;
        this.autor = autor;
        this.texto = texto;
        this.fecha = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    public static int getCont() { return cont; }
    public static void setCont(int cont) { Comentario.cont = cont; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getProductoId() { return productoId; }
    public void setProductoId(int productoId) { this.productoId = productoId; }

    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
}