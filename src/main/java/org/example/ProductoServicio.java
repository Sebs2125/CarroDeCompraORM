package org.example;

/*
Puntos 5 y 8
CRUD y imagenes base64, junto a paginación
 */

import java.util.List;

public class ProductoServicio
{
    private final ProductoConsulta consulta = new ProductoConsulta ();

    //Crear producto
    public void crearProducto( Producto producto )
    {
        consulta.crear( producto );
    }

    //Buscar producto por id
    public Producto buscarPorId( Long id )
    {
        return consulta.buscarPorId( id );
    }

    //Actualizar producto
    public void actualizarProducto( Producto producto )
    {
        consulta.actualizar( producto );
    }

    //Eliminar producto
    public void eliminarProducto( Long id )
    {
        consulta.eliminar(id);
    }

    //Obtener todos los productos
    public List<Producto> obtenerTodosLosProductos()
    {
        return consulta.obtenerTodos();
    }

    //Obtener productos paginados (10 por pagina)
    public List<Producto> obtenerTodosPorUsuario( int page )
    {
        return consulta.paginated(page, 10);
    }

}
