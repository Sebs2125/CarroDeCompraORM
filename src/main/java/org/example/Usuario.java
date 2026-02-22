package org.example;

import jakarta.persistence.*;

/*
Puntos 3 y 4:
3- Se trabaja con modelos de datos para las autenticaciones.
4- Se puede recordar el usuario con las cookies (todo referido a servicio).
 */

@Entity
public class Usuario
{

    @Id
    @GeneratedValue( strategy =  GenerationType.IDENTITY )
    private Long id;

    @Column(unique = true)
    private String usuario;
    private String password;
    private boolean confirmarAdmin;

    @OneToMany( mappedBy = "usuario", cascade = CascadeType.ALL )
    private List<CarritoItem> carrito;

    @OneToMany( mappedBy = "usuario", cascade = CascadeType.ALL )
    private List<Comentario> comentarios;

    public Usuario()
    {
    }

    //Constructor
    public Usuario( String usuario, String password, boolean confirmarAdmin )
    {
        this.usuario = usuario;
        this.password = password;
        this.confirmarAdmin = confirmarAdmin;
    }

    //Getter and Setter
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isConfirmarAdmin() {
        return confirmarAdmin;
    }

    public void setConfirmarAdmin(boolean confirmarAdmin) {
        this.confirmarAdmin = confirmarAdmin;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

}
