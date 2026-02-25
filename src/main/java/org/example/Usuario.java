package org.example;

import jakarta.persistence.*;

@Entity
@Table(name = "usuario")
public class Usuario
{
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    private Long id;

    @Column(name = "usuario")
    private String usuario;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "password")
    private String password;

    @Column(name = "confirmarAdmin")
    private boolean confirmarAdmin;

    public Usuario() {}

    public Usuario( String usuario, String nombre, String password, boolean confirmarAdmin )
    {
        this.usuario = usuario;
        this.nombre = nombre;
        this.password = password;
        this.confirmarAdmin = confirmarAdmin;
    }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public boolean isConfirmarAdmin() { return confirmarAdmin; }
    public void setConfirmarAdmin(boolean confirmarAdmin) { this.confirmarAdmin = confirmarAdmin; }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}