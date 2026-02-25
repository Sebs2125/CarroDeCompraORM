package org.example;

import io.javalin.http.Context;

import java.util.HashMap;
import java.util.Map;

public class CarroControlador {
    private ProductoGestion productoGestion = ProductoGestion.getInstance();

    public void addAlCarro(Context ctx) {
        try {

            String paramProdId = ctx.formParam("productoId");
            String paramCantidad = ctx.formParam("cantidad");

            if (paramProdId == null || paramProdId.isEmpty() || paramProdId.equals("null") || paramProdId.equals("undefined")) {
                ctx.status(400).result("ID de producto inválido");
                return;
            }
            if (paramCantidad == null || paramCantidad.isEmpty() || paramCantidad.equals("null") || paramCantidad.equals("undefined")) {
                ctx.status(400).result("Cantidad inválida");
                return;
            }

            Long productoId = Long.parseLong(paramProdId);
            int cantidad = Integer.parseInt(paramCantidad);

            Producto producto = productoGestion.obtenerProuctoPorId(productoId).orElse(null);

            if (producto == null) {
                ctx.status(404).result("El producto no existe");
                return;
            }

            if (cantidad <= 0) {
                ctx.status(400).result("Cantidad invalida");
                return;
            }

            if (!productoGestion.poseeInventario(productoId, cantidad)) {
                ctx.status(400).result("Inventario insuficiente");
                return;
            }

            CarritoDeCompra carro = ctx.sessionAttribute("carro");

            if (carro == null) {
                carro = new CarritoDeCompra();
                ctx.sessionAttribute("carro", carro);
            }

            for (int ind = 0; ind < cantidad; ind++) {
                carro.agregarProducto(producto);
            }

            productoGestion.reducirInventario(productoId, cantidad);

            System.out.println("Producto agregado al carrito - ID: " + productoId + ", Cantidad: " + cantidad);
            System.out.println("Total productos en carrito: " + carro.getCantidadProductos());
            ctx.redirect("/productos");

        } catch (Exception e) {
            System.out.println("Error al agregar al carrito: " + e.getMessage());
            ctx.status(400).result("Error al agregar al carrito");
        }
    }

    public void mostrarCarro(Context ctx) {
        CarritoDeCompra carro = ctx.sessionAttribute("carro");

        if (carro == null) {
            carro = new CarritoDeCompra();
        }

        Map<String, Object> modelo = new HashMap<>();
        modelo.put("carro", carro);
        modelo.put("productos", carro.getListaProductos());
        modelo.put("total", carro.getTotal());
        modelo.put("CantidadProducto", carro.getCantidadProductos());

        Usuario usuarioActual = ctx.sessionAttribute("usuarioActual");
        modelo.put("user", usuarioActual);

        ctx.render("/templates/carrito.html", modelo);

    }

    public void limpiarCarro(Context ctx) {
        CarritoDeCompra carro = ctx.sessionAttribute("carro");

        if (carro != null) {
            carro.limpiar();
            System.out.println("Carrito limpio");
        }

        ctx.redirect("/carrito");

    }

}
