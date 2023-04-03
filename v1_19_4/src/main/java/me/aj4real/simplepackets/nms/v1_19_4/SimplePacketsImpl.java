/**********************************
 Copyright (c) All Rights Reserved
 *********************************/

package me.aj4real.simplepackets.nms.v1_19_4;

import io.netty.channel.ChannelFuture;
import me.aj4real.simplepackets.*;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerConnectionListener;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R3.CraftServer;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class SimplePacketsImpl implements SimplePackets {
    @Override
    public void send(Object connection, Object packet) {
        assert (packet != null && connection != null);
        assert (packet instanceof Packet && connection instanceof Connection);
        ((Connection)connection).send((Packet)packet);
    }
    public void onEnable(Plugin plugin) {
        try {
            String findChannelsField = List.class.getCanonicalName() + "<" + ChannelFuture.class.getCanonicalName() + ">";
            Field channelsField = Arrays.stream(ServerConnectionListener.class.getDeclaredFields()).filter((f) -> f.getGenericType().getTypeName().equalsIgnoreCase(findChannelsField)).findFirst().get();
            channelsField.setAccessible(true);
            ServerConnectionListener con = ((CraftServer) Bukkit.getServer()).getHandle().getServer().getConnection();
            List<ChannelFuture> futures = (List<ChannelFuture>) channelsField.get(con);
            synchronized (((CraftServer) Bukkit.getServer()).getHandle().getServer().getConnection()) {
                TheUnsafe.get().putObject(
                        con,
                        TheUnsafe.get().objectFieldOffset(channelsField),
                        new ProxyList<>(futures, Packets::inject, (ch) -> {})
                );
            }

            String findConnectionsField = List.class.getCanonicalName() + "<" + Connection.class.getCanonicalName() + ">";
            Field connectionsField = Arrays.stream(ServerConnectionListener.class.getDeclaredFields()).filter((f) -> f.getGenericType().getTypeName().equalsIgnoreCase(findConnectionsField)).findAny().get();
            connectionsField.setAccessible(true);
            synchronized (((CraftServer) Bukkit.getServer()).getHandle().getServer().getConnection()) {
                List<Connection> connections = con.getConnections();
                TheUnsafe.get().putObject(
                        con,
                        TheUnsafe.get().objectFieldOffset(connectionsField),
                        new ProxyList<>(connections, (c) -> {
                            if(!c.preparing) Client.getFromChannel(c.channel).setConnection(c);
                        }, (c) -> {})
                );
            }

            String findServerPlayersField = List.class.getCanonicalName() + "<" + ServerPlayer.class.getCanonicalName() + ">";
            Field serverPlayersField = Arrays.stream(PlayerList.class.getDeclaredFields()).filter(f -> f.getGenericType().getTypeName().equalsIgnoreCase(findServerPlayersField)).findAny().get();
            serverPlayersField.setAccessible(true);
            PlayerList playerList = ((CraftServer)Bukkit.getServer()).getHandle();
            List<ServerPlayer> players = playerList.players;
            Field connectionField = Arrays.stream(ServerGamePacketListenerImpl.class.getDeclaredFields()).filter(f -> f.getGenericType().getTypeName().equals(Connection.class.getCanonicalName())).findAny().get();
            connectionField.setAccessible(true);
            synchronized (((CraftServer)Bukkit.getServer()).getHandle()) {
                TheUnsafe.get().putObject(
                        playerList,
                        TheUnsafe.get().objectFieldOffset(serverPlayersField),
                        new ProxyList<>(players, (p) -> {
                            try {
                                Client.getFromChannel(((Connection)connectionField.get(p.connection)).channel).setConnection(connectionField.get(p.connection));
                                Client.getFromConnection(connectionField.get(p.connection)).setPlayer(p.getBukkitEntity().getPlayer());
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                            }, (p) -> {})
                );

            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(plugin);
        }
    }
}
