package org.example;

/*
Punto 6.
Consulta para items del carrito.
 */

import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;


public class CarritoItemConsulta
{
    public void crear( CarritoItem item )
    {
        Session sesion = HibernateConsulta.getSessionFactory().openSession();
        Transaction tx = sesion.beginTransaction();
        sesion.persist( item );
        tx.commit();
        sesion.close();
    }

    public CarritoItem buscarPorId( Long id )
    {
        Session sesion = HibernateConsulta.getSessionFactory().openSession();
        CarritoItem item = sesion.get(CarritoItem.class, id );
        sesion.close();
        return item;
    }

    public void actualizar( CarritoItem item )
    {
        Session sesion = HibernateConsulta.getSessionFactory().openSession();
        Transaction tx = sesion.beginTransaction();
        sesion.merge(item);
        tx.commit();
        sesion.close();
    }

    public void eliminar( Long id )
    {
        Session sesion = HibernateConsulta.getSessionFactory().openSession();
        Transaction tx = sesion.beginTransaction();
        CarritoItem item = sesion.get( CarritoItem.class, id );

        if ( item != null ) sesion.remove( item );
        tx.commit();
        sesion.close();

    }

    public List<CarritoItem> obtenerPorUsuario( Long usuarioId )
    {
        Session sesion = HibernateConsulta.getSessionFactory().openSession();
        List<CarritoItem> lista = sesion.createQuery("FROM CarritoItem i WHERE i.usuario.id = :usuarioId", CarritoItem.class )
                .setParameter("usuarioId", usuarioId )
                .getResultList();
        sesion.close();
        return lista;
    }

}
