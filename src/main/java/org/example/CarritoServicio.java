package org.example;

/*
Punto 6
Servicio para gestión del carrito de compra.
 */

import java.util.List;

public class CarritoServicio
{
    private final CarritoItemConsulta carritoItemConsulta = new CarritoItemConsulta();

    //Agregar item al carrito
    public void agregarItem( CarritoItem item )
    {
        carritoItemConsulta.crear( item );
    }

    //Obtener items del carrito de un usuario
    public List<CarritoItem> obtenerItemsPorUsuario(Long usuarioId )
    {
        return carritoItemConsulta.obtenerPorUsuario(usuarioId);
    }

    //Eliminar item del carrito
    public void eliminarItem ( Long id )
    {
        carritoItemConsulta.eliminar(id);
    }

    //Actualizar item
    public void actualizarItem ( CarritoItem item )
    {
        carritoItemConsulta.actualizar(item);
    }

}
