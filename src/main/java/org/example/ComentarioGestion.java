package org.example;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ComentarioGestion {

    private static ComentarioGestion instance;

    // productoId → lista de comentarios
    private final Map<Integer, List<Comentario>> comentariosPorProducto = new ConcurrentHashMap<>();

    private ComentarioGestion() {
        // Comentarios de ejemplo
        agregarComentario(new Comentario(1, "Sebastian", "Excelente RAM, muy rápida."));
        agregarComentario(new Comentario(1, "Esmil", "Buena relación calidad-precio."));
        agregarComentario(new Comentario(2, "admin", "Computadora muy potente para trabajo."));
    }

    public static synchronized ComentarioGestion getInstance() {
        if (instance == null) {
            instance = new ComentarioGestion();
        }
        return instance;
    }

    public void agregarComentario(Comentario comentario) {
        comentariosPorProducto
                .computeIfAbsent(comentario.getProductoId(), k -> Collections.synchronizedList(new ArrayList<>()))
                .add(comentario);
    }

    public List<Comentario> getComentariosPorProducto(int productoId) {
        return comentariosPorProducto.getOrDefault(productoId, Collections.emptyList());
    }

    public Optional<Comentario> buscarPorId(int id) {
        return comentariosPorProducto.values().stream()
                .flatMap(List::stream)
                .filter(c -> c.getId() == id)
                .findFirst();
    }

    /**
     * Elimina el comentario y retorna el productoId al que pertenecía,
     * o -1 si no existe.
     */
    public int eliminarComentario(int comentarioId) {
        for (Map.Entry<Integer, List<Comentario>> entry : comentariosPorProducto.entrySet()) {
            boolean removed = entry.getValue().removeIf(c -> c.getId() == comentarioId);
            if (removed) {
                System.out.println("Comentario eliminado ID: " + comentarioId + " del producto: " + entry.getKey());
                return entry.getKey();
            }
        }
        return -1;
    }
}