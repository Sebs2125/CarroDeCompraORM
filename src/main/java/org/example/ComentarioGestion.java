package org.example;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ComentarioGestion {

    private static ComentarioGestion instance;

    // productoId (int) -> lista de comentarios en memoria para WS
    private final Map<Integer, List<Comentario>> comentariosPorProducto = new ConcurrentHashMap<>();

    private ComentarioGestion() {
    }

    public static synchronized ComentarioGestion getInstance() {
        if (instance == null) {
            instance = new ComentarioGestion();
        }
        return instance;
    }

    public void agregarComentario(Comentario comentario) {
        int productoId = comentario.getProductoId();
        comentariosPorProducto
                .computeIfAbsent(productoId, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(comentario);
    }

    public List<Comentario> getComentariosPorProducto(int productoId) {
        return comentariosPorProducto.getOrDefault(productoId, Collections.emptyList());
    }

    public Optional<Comentario> buscarPorId(Long id) {
        return comentariosPorProducto.values().stream()
                .flatMap(List::stream)
                .filter(c -> c.getId() != null && c.getId().equals(id))
                .findFirst();
    }

    public int eliminarComentario(Long comentarioId) {
        for (Map.Entry<Integer, List<Comentario>> entry : comentariosPorProducto.entrySet()) {
            boolean removed = entry.getValue()
                    .removeIf(c -> c.getId() != null && c.getId().equals(comentarioId));
            if (removed) {
                System.out.println("Comentario eliminado del cache ID: "
                        + comentarioId + " del producto: " + entry.getKey());
                return entry.getKey();
            }
        }
        return -1;
    }
}