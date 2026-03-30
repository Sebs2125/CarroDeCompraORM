package org.example;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class VentaGestion {

    private static VentaGestion instance;
    private static int ventaCounter = 0;

    private final List<Venta> ventas = new CopyOnWriteArrayList<>();
    private final WebSocketGestion wsGestion;

    private VentaGestion() {
        this.wsGestion = WebSocketGestion.getInstance();
    }

    public static synchronized VentaGestion getInstance() {
        if (instance == null) {
            instance = new VentaGestion();
        }
        return instance;
    }

    /**
     * Registra una nueva venta y notifica al dashboard en tiempo real.
     */
    public Venta registrarVenta(String nombreCliente, List<Producto> productos) {
        Venta venta = new Venta(++ventaCounter, nombreCliente, new ArrayList<>(productos));
        ventas.add(venta);

        System.out.println("Venta registrada #" + venta.getId() + " para: " + nombreCliente
                + " | Total: RD$ " + venta.getTotal());

        // ── WebSocket: push al dashboard sin polling ──────────────────────────
        wsGestion.broadcastDashboardUpdate(this);

        return venta;
    }

    public List<Venta> getVentas() {
        return Collections.unmodifiableList(ventas);
    }

    /** Suma total de todas las ventas */
    public double getTotalVentas() {
        return ventas.stream().mapToDouble(Venta::getTotal).sum();
    }

    /**
     * Retorna un mapa nombre→cantidad para el gráfico de torta.
     * Cada unidad en el carrito cuenta como 1.
     */
    public Map<String, Integer> getProductosMasVendidos() {
        Map<String, Integer> conteo = new LinkedHashMap<>();
        ventas.stream()
                .flatMap(v -> v.getProductos().stream())
                .forEach(p -> conteo.merge(p.getNombre(), 1, Integer::sum));

        // Ordenar por cantidad descendente
        return conteo.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    /** Serialización simple para la carga inicial del dashboard */
    public Map<String, Object> getDashboardData() {
        Map<String, Object> data = new HashMap<>();
        data.put("totalVentas", getTotalVentas());
        data.put("cantidadVentas", ventas.size());
        data.put("productosMasVendidos", getProductosMasVendidos());
        return data;
    }
}