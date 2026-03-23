package org.example;


import io.javalin.websocket.WsContext;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static javax.management.Query.or;


public class Websocketgestor {


    private static  final  Set<WsContext> usuariosConectados = ConcurrentHashMap.newKeySet();

    private static  final  ConcurrentHashMap<Long,Set<WsContext>> conexionesPorproductos = new ConcurrentHashMap<>();

    public static void conectar(WsContext ctx) {
        usuariosConectados.add(ctx);
        broadcastContador();
    }


    public static void desconectar(WsContext ctx){
        usuariosConectados.remove(ctx);

        conexionesPorproductos.values().forEach(set -> set.remove(ctx));
        broadcastContador();

    }


    private static void broadcastContador(){


            int total = usuariosConectados.size();
            String mensaje = "{\"tipo\":\"contador\",\"usuarios\":" + total + "}";

            usuariosConectados.forEach(ctx -> {
                if (ctx.session.isOpen()) {
                    ctx.send(mensaje);
                }
            });





    }

    public static void registrarVistaProducto(WsContext ctx, Long productoId)
    {


        conexionesPorproductos.computeIfAbsent(productoId, k->ConcurrentHashMap.newKeySet()).add(ctx);



    }

    public static void notificarComentarioEliminado(Long productoId, Long comentarioId)
    {
        Set<WsContext> viendo = conexionesPorproductos.get(productoId);
                    if(viendo ==null)return;

                    String mensaje  = "{\"tipo\":\"comentarioEliminado\",\"comentarioId\":" + comentarioId + "}";

        viendo.forEach(ctx->

        {
            if(ctx.session.isOpen())
            {ctx.send(mensaje);}
        });





    }

}

