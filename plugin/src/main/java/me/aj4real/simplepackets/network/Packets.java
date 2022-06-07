/**********************************
 Copyright (c) All Rights Reserved
 *********************************/

package me.aj4real.simplepackets.network;

import io.netty.channel.*;

import java.util.HashMap;
import java.util.Map;

public class Packets {
    public static Map<Class, Handler> handlers = new HashMap<>();

    public static <T> void addHandler(Class<T> clazz, Handler<T> handler) {
        handlers.put(clazz, handler);
    }

    public static void inject(ChannelFuture connection) {
        final ChannelInboundHandler finishProtocol = new ChannelInitializer<>() {
            @Override
            protected void initChannel(final Channel channel) {
                Object o = channel.eventLoop().submit(() -> {
                    channel.pipeline()
                            .addBefore("packet_handler", "tagseditor", Client.getFromChannel(channel))
                            .addLast(new Disconnection());
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
    }
    @FunctionalInterface
    public interface Handler<T> {
        T handle(Client player, T packet);
    }

    @ChannelHandler.Sharable
    private static class Disconnection extends ChannelInboundHandlerAdapter {
        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) {
            Client.getFromChannel(ctx.channel()).dispose();
        }
    }
}
