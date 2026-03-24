package org.example;

import io.javalin.websocket.WsContext;
import com.google.gson.Gson;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Hub central de WebSockets.
 * Maneja tres tipos de canales:
 *   - "users"     → transmite conteo de usuarios logueados en tiempo real
 *   - "comments"  → transmite eliminación de comentarios en tiempo real
 *   - "dashboard" → transmite actualizaciones de ventas en tiempo real
 */
public class WebSocketGestion {

    private static WebSocketGestion instance;
    private final Gson gson = new Gson();

    // Mapa: sessionId → contexto WS
    // Separamos los suscriptores por canal para no enviar mensajes a quien no corresponde

    // Canal: conteo de usuarios conectados
    private final Map<String, WsContext> userCountSubs = new ConcurrentHashMap<>();

    // Canal: comentarios por producto  productId → Set de contextos suscritos
    private final Map<Integer, Set<WsContext>> commentSubs = new ConcurrentHashMap<>();

    // Canal: dashboard de ventas
    private final Map<String, WsContext> dashboardSubs = new ConcurrentHashMap<>();

    // Usuarios logueados activos: sessionId → nombre
    private final Map<String, String> activeUsers = new ConcurrentHashMap<>();

    private WebSocketGestion() {}

    public static synchronized WebSocketGestion getInstance() {
        if (instance == null) {
            instance = new WebSocketGestion();
        }
        return instance;
    }

    // ─────────────────────────────────────────────
    //  Suscripciones
    // ─────────────────────────────────────────────

    public void subscribeUserCount(WsContext ctx) {
        userCountSubs.put(ctx.getSessionId(), ctx);
        broadcastUserCount(); // enviar conteo actual al nuevo suscriptor
    }

    public void unsubscribeUserCount(WsContext ctx) {
        userCountSubs.remove(ctx.getSessionId());
    }

    public void subscribeComments(WsContext ctx, int productId) {
        commentSubs.computeIfAbsent(productId, k -> ConcurrentHashMap.newKeySet()).add(ctx);
    }

    public void unsubscribeComments(WsContext ctx, int productId) {
        Set<WsContext> subs = commentSubs.get(productId);
        if (subs != null) {
            subs.remove(ctx);
        }
    }

    public void subscribeDashboard(WsContext ctx) {
        dashboardSubs.put(ctx.getSessionId(), ctx);
    }

    public void unsubscribeDashboard(WsContext ctx) {
        dashboardSubs.remove(ctx.getSessionId());
    }

    // ─────────────────────────────────────────────
    //  Gestión de usuarios activos
    // ─────────────────────────────────────────────

    public void userLoggedIn(String sessionId, String username) {
        activeUsers.put(sessionId, username);
        broadcastUserCount();
    }

    public void userLoggedOut(String sessionId) {
        activeUsers.remove(sessionId);
        broadcastUserCount();
    }

    public int getActiveUserCount() {
        return activeUsers.size();
    }

    // ─────────────────────────────────────────────
    //  Broadcasts
    // ─────────────────────────────────────────────

    /** Envía el conteo actualizado a todos los suscriptores del canal users */
    public void broadcastUserCount() {
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", "user_count");
        msg.put("count", activeUsers.size());
        String json = gson.toJson(msg);

        userCountSubs.values().removeIf(ctx -> !ctx.session.isOpen());
        userCountSubs.values().forEach(ctx -> {
            try { ctx.send(json); } catch (Exception ignored) {}
        });
    }

    /**
     * Notifica a todos los usuarios que ven un producto que se eliminó un comentario.
     * @param productId  id del producto afectado
     * @param commentId  id del comentario eliminado
     */
    public void broadcastCommentDeleted(int productId, int commentId) {
        Set<WsContext> subs = commentSubs.get(productId);
        if (subs == null || subs.isEmpty()) return;

        Map<String, Object> msg = new HashMap<>();
        msg.put("type", "comment_deleted");
        msg.put("productId", productId);
        msg.put("commentId", commentId);
        String json = gson.toJson(msg);

        subs.removeIf(ctx -> !ctx.session.isOpen());
        subs.forEach(ctx -> {
            try { ctx.send(json); } catch (Exception ignored) {}
        });
    }

    /**
     * Envía la actualización de ventas al dashboard en tiempo real.
     * @param ventaGestion la gestión con los datos actualizados
     */
    public void broadcastDashboardUpdate(VentaGestion ventaGestion) {
        dashboardSubs.values().removeIf(ctx -> !ctx.session.isOpen());
        if (dashboardSubs.isEmpty()) return;

        Map<String, Object> msg = new HashMap<>();
        msg.put("type", "dashboard_update");
        msg.put("totalVentas", ventaGestion.getTotalVentas());
        msg.put("cantidadVentas", ventaGestion.getVentas().size());
        msg.put("productosMasVendidos", ventaGestion.getProductosMasVendidos());
        String json = gson.toJson(msg);

        dashboardSubs.values().forEach(ctx -> {
            try { ctx.send(json); } catch (Exception ignored) {}
        });
    }

    /**
     * Envía un mensaje JSON ya construido a todos los suscriptores del canal
     * de comentarios de un producto (usado para nuevos comentarios).
     */
    public void broadcastComentarioAgregado(int productId, String json) {
        Set<WsContext> subs = commentSubs.get(productId);
        if (subs == null || subs.isEmpty()) return;
        subs.removeIf(ctx -> !ctx.session.isOpen());
        subs.forEach(ctx -> {
            try { ctx.send(json); } catch (Exception ignored) {}
        });
    }

    // Limpieza general al cerrar una conexión (cualquier canal)
    public void cleanupSession(String sessionId) {
        userCountSubs.remove(sessionId);
        dashboardSubs.remove(sessionId);
        commentSubs.values().forEach(set ->
                set.removeIf(ctx -> ctx.getSessionId().equals(sessionId))
        );
    }
}