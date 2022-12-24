/**********************************
 Copyright (c) All Rights Reserved
 *********************************/

package me.aj4real.simplepackets;

import io.netty.channel.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Packets {
    public static Map<Class<?>, List<Handler>> handlers = new HashMap<>();
    private static Map<Handler, Class> _handlers = new HashMap<>();
    public static Map<ChannelFuture, ChannelHandler> cache = new HashMap<>();

    public static String identifier = "simplepackets";
    @SuppressWarnings("unused")
    public static <T> Handler<T> addHandler(Class<T> clazz, Handler<T> handler) {
        handlers.computeIfAbsent(clazz, (c) -> new ArrayList<>()).add(handler);
        _handlers.put(handler, clazz);
        return handler;
    }
    private static Disconnection disconnection = new Disconnection();

    public static void inject(ChannelFuture connection) {
        final ChannelInboundHandler finishProtocol = new ChannelInitializer<>() {
            @Override
            protected void initChannel(final Channel channel) {
                channel.eventLoop().submit(() -> {
                    channel.pipeline()
                            .addBefore("packet_handler", identifier, Client.getFromChannel(channel))
                            .addLast(disconnection);
                });
            }
        };
        final ChannelInboundHandler startProtocol = new ChannelInitializer<>() {
            @Override
            protected void initChannel(Channel channel) {
                channel.pipeline().addLast(finishProtocol);
            }
        };
        final ChannelHandler connectionHandler = new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                Channel channel = (Channel) msg;
                channel.pipeline().addFirst(startProtocol);
                ctx.fireChannelRead(msg);
            }
        };
        connection.channel().eventLoop().submit(() -> {
            connection.channel().pipeline().addFirst(connectionHandler);
        });
        cache.put(connection, connectionHandler);
    }

    public static void onEnable(Plugin plugin) {
        identifier = plugin.getName().toLowerCase();
    }
    public static void onDisable(@SuppressWarnings("unused") Plugin unused) {
        Client.fromChannel.values().forEach((v) -> {
            v.channel.pipeline().remove(v);
        });
        cache.forEach((k,v) -> k.channel().pipeline().remove(v));
    }

    @FunctionalInterface
    public interface Handler<T> {
        T handle(Client<?> player, T packet);
        default void dispose() {
            if(!_handlers.containsKey(this)) return;
            if(!handlers.containsKey(_handlers.get(this))) return;
            handlers.get(_handlers.get(this)).remove(this);
            //TODO
        }
    }

    @ChannelHandler.Sharable
    private static class Disconnection extends ChannelInboundHandlerAdapter {
        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) {
            Client.getFromChannel(ctx.channel()).dispose();
        }
    }
}
