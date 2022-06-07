/**********************************
 Copyright (c) All Rights Reserved
 *********************************/

package me.aj4real.simplepackets.nms.v1_18_R2;

import io.netty.channel.ChannelFuture;
import me.aj4real.simplepackets.NMS;
import me.aj4real.simplepackets.network.*;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerConnectionListener;
import net.minecraft.server.players.PlayerList;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_18_R2.CraftServer;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

public class NMSImpl implements NMS {

    public void onEnable(Plugin plugin) {
        try {
            Client.sender = (BiConsumer<Connection, Packet>) (Connection::send);

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
                        new ProxyList<>(connections, (c) -> Client.getFromChannel(c.channel).setConnection(c), (c) -> {})
                );
            }

            String findServerPlayersField = List.class.getCanonicalName() + "<" + ServerPlayer.class.getCanonicalName() + ">";
            Field serverPlayersField = Arrays.stream(PlayerList.class.getDeclaredFields()).filter(f -> f.getGenericType().getTypeName().equalsIgnoreCase(findServerPlayersField)).findAny().get();
            serverPlayersField.setAccessible(true);
            PlayerList playerList = ((CraftServer)Bukkit.getServer()).getHandle();
            List<ServerPlayer> players = playerList.players;
            synchronized (((CraftServer)Bukkit.getServer()).getHandle()) {
                TheUnsafe.get().putObject(
                        playerList,
                        TheUnsafe.get().objectFieldOffset(serverPlayersField),
                        new ProxyList<>(players,
                                (p) -> Client.getFromConnection(p.connection.connection).setPlayer(p.getBukkitEntity().getPlayer()),
                                (p) -> {
                                })
                );

            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(plugin);
        }
    }
}
