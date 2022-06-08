/**********************************
 Copyright (c) All Rights Reserved
 *********************************/

package me.aj4real.simplepackets;

import io.netty.channel.*;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@ChannelHandler.Sharable
public class Client<C> extends ChannelDuplexHandler {
    public static BiConsumer sender = null;
    private Consumer<Player> playerWaiter = null;
    private static final Map<Channel, Client> fromChannel = new HashMap<>();
    private static final Map<Object, Client> fromConnection = new HashMap<>();
    public static final Map<Player, Client> fromPlayer = new HashMap<>();
    private final Channel channel;
    private Player player = null;
    private C connection = null;

    public static Client getFromChannel(Channel channel) {
        Client c = fromChannel.get(channel);
        if(c == null) return new Client(channel);
        return c;
    }

    public static Client getFromConnection(Object connection) {
        return fromConnection.get(connection);
    }
    public static Client getFromPlayer(Player player) {
        return fromPlayer.get(player);
    }

    private Client(Channel channel) {
        this.channel = channel;
        fromChannel.put(channel, this);
    }
    public void dispose() {
        fromChannel.remove(this.channel);
        if(this.connection != null)
            fromConnection.remove(this.connection);
        if(this.player != null)
            fromPlayer.remove(this.player);
    }
    public Player getPlayer() {
        return this.player;
    }
    public C getConnection() {
        return this.connection;
    }
    public void sendPacket(Object packet) {
        sender.accept(connection, packet);
    }
    public void setPlayer(Player player) {
        this.player = player;
        fromPlayer.put(player, this);
        synchronized (this) {
            if(this.playerWaiter != null) this.playerWaiter.accept(player);
        }
    }
    public void waitForPlayer(Consumer<Player> c) {
        synchronized (this) {
            if(this.player == null) this.playerWaiter = c;
            else c.accept(this.player);
        }
    }
    public void setConnection(C connection) {
        this.connection = connection;
        fromConnection.put(connection, this);
    }
    private <T> T handle(T msg) {
        if(msg == null) return null;
        Packets.Handler<T> handler = Packets.handlers.get(msg.getClass());
        if(handler != null) {
            try {
                msg = handler.handle(this, msg);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return msg;
    }
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        msg = handle(msg);
        if(msg != null) super.write(ctx, msg, promise);
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        msg = handle(msg);
        if(msg != null) super.channelRead(ctx, msg);
    }
}
