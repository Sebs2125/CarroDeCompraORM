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
public class WebSocketGestion
{

    private static WebSocketGestion instance;
    private final Gson gson = new Gson();

    // Canal: conteo de usuarios conectados  sessionId -> WsContext
    private final Map<String, WsContext> userCountSubs = new ConcurrentHashMap<>();

    // Canal: comentarios por producto  productId -> Set<WsContext>
    private final Map<Integer, Set<WsContext>> commentSubs = new ConcurrentHashMap<>();

    // Canal: dashboard de ventas  sessionId -> WsContext
    private final Map<String, WsContext> dashboardSubs = new ConcurrentHashMap<>();

    private final Map<String, Set<String>> activeUsers = new ConcurrentHashMap<>();

    // Índice inverso para poder buscar el username de un sessionId en O(1)
    private final Map<String, String> sessionToUser = new ConcurrentHashMap<>();

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
        broadcastUserCount();
    }

    public void unsubscribeUserCount(WsContext ctx) {
        userCountSubs.remove(ctx.getSessionId());
    }

    public void subscribeComments(WsContext ctx, int productId) {
        commentSubs.computeIfAbsent(productId, k -> ConcurrentHashMap.newKeySet()).add(ctx);
    }

    public void unsubscribeComments(WsContext ctx, int productId) {
        Set<WsContext> subs = commentSubs.get(productId);
        if (subs != null) subs.remove(ctx);
    }

    public void subscribeDashboard(WsContext ctx) {
        dashboardSubs.put(ctx.getSessionId(), ctx);
    }

    public void unsubscribeDashboard(WsContext ctx) {
        dashboardSubs.remove(ctx.getSessionId());
    }

    // ─────────────────────────────────────────────
    //  Gestión de usuarios activos (CORREGIDO)
    // ─────────────────────────────────────────────

    public void userLoggedIn(String sessionId, String username) {
        if (username == null || username.isBlank()) return;

        // Si este sessionId ya tenía otro usuario asociado, limpiarlo primero
        String prevUser = sessionToUser.put(sessionId, username);
        if (prevUser != null && !prevUser.equals(username)) {
            removeSessionFromUser(sessionId, prevUser);
        }

        // Agregar sessionId al Set del usuario
        activeUsers.computeIfAbsent(username, k -> ConcurrentHashMap.newKeySet()).add(sessionId);

        broadcastUserCount();
    }

    public void userLoggedOut(String sessionId) {
        String username = sessionToUser.remove(sessionId);
        if (username != null) {
            removeSessionFromUser(sessionId, username);
            broadcastUserCount();
        }
    }

    /** Elimina sessionId del Set del usuario; si queda vacío, elimina la entrada del mapa. */
    private void removeSessionFromUser(String sessionId, String username) {
        Set<String> sessions = activeUsers.get(username);
        if (sessions != null) {
            sessions.remove(sessionId);
            if (sessions.isEmpty()) {
                activeUsers.remove(username, sessions);
            }
        }
    }

    /** Cantidad de usuarios ÚNICOS logueados (no número de tabs/conexiones WS). */
    public int getActiveUserCount() {
        return activeUsers.size();
    }

    // ─────────────────────────────────────────────
    //  Broadcasts
    // ─────────────────────────────────────────────

    public void broadcastUserCount() {
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", "user_count");
        msg.put("count", activeUsers.size());
        String json = gson.toJson(msg);

        userCountSubs.values().forEach(ctx -> {
            try { ctx.send(json); } catch (Exception ignored) {}
        });
    }

    public void broadcastCommentDeleted(int productId, int commentId) {
        Set<WsContext> subs = commentSubs.get(productId);
        if (subs == null || subs.isEmpty()) return;

        Map<String, Object> msg = new HashMap<>();
        msg.put("type", "comment_deleted");
        msg.put("productId", productId);
        msg.put("commentId", commentId);
        String json = gson.toJson(msg);

        subs.forEach(ctx -> {
            try { ctx.send(json); } catch (Exception ignored) {}
        });
    }

    public void broadcastDashboardUpdate(VentaGestion ventaGestion) {
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

    public void broadcastComentarioAgregado(int productId, String json) {
        Set<WsContext> subs = commentSubs.get(productId);
        if (subs == null || subs.isEmpty()) return;

        subs.forEach(ctx -> {
            try { ctx.send(json); } catch (Exception ignored) {}
        });
    }

    public void cleanupSession(String sessionId) {
        // Limpiar usuario activo
        userLoggedOut(sessionId);

        // Limpiar suscripciones
        userCountSubs.remove(sessionId);
        dashboardSubs.remove(sessionId);
        commentSubs.values().forEach(set ->
                set.removeIf(ctx -> ctx.getSessionId().equals(sessionId))
        );
    }
}