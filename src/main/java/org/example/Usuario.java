package org.example;

public class Usuario
{

    private String usuario;
    private String nombre;
    private String password;
    private boolean confirmarAdmin;

    public Usuario()
    {
    }

    //Constructor
    public Usuario(String usuario, String nombre, String password, boolean confirmarAdmin)
    {
        this.usuario = usuario;
        this.nombre = nombre;
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
