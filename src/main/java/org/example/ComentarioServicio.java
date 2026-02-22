package org.example;

/*
Puntos 6 y 7
Servicio de gestion de comentarios en producto.
 */

import java.util.List;

public class ComentarioServicio
{
    private final ComentarioConsulta comentarioConsulta = new ComentarioConsulta();

    //Agregar comentario a producto
    public void agregarComentario ( Comentario comentario )
    {
        comentarioConsulta.crear( comentario );
    }

    //Buscar comentario por id
    public Comentario buscarPorId ( long id )
    {
        return comentarioConsulta.buscarPorId( id );
    }

    //Obtener comentarios por producto
    public List<Comentario> obtenerPorProducto(Long productoId )
    {
        return comentarioConsulta.comentariosPorProducto( productoId );
    }

    //Actualizar comentario (puede ser editar contenido)
    public void actualizarComentario( Comentario comentario )
    {
        comentarioConsulta.actualizar( comentario );
    }

    //Eliminar comentario (solo admin)
    public void eliminarComentario( Long id, boolean esAdmin )
    {
        if ( esAdmin ) comentarioConsulta.eliminar( id );
    }

}
