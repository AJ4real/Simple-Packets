/**********************************
 Copyright (c) All Rights Reserved
 *********************************/

package me.aj4real.simplepackets.nms.v1_14_R1;

import io.netty.channel.ChannelFuture;
import me.aj4real.simplepackets.NMS;
import me.aj4real.simplepackets.network.Client;
import me.aj4real.simplepackets.network.Packets;
import me.aj4real.simplepackets.network.ProxyList;
import me.aj4real.simplepackets.network.TheUnsafe;
import net.minecraft.server.v1_14_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_14_R1.CraftServer;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

public class NMSImpl implements NMS {

    public void onEnable(Plugin plugin) {

        try {
            Client.sender = (BiConsumer<NetworkManager, Packet>) (NetworkManager::sendPacket);

            String findChannelsField = List.class.getCanonicalName() + "<" + ChannelFuture.class.getCanonicalName() + ">";
            Field channelsField = Arrays.stream(ServerConnection.class.getDeclaredFields()).filter((f) -> f.getGenericType().getTypeName().equalsIgnoreCase(findChannelsField)).findFirst().get();
            channelsField.setAccessible(true);
            ServerConnection con = ((CraftServer) Bukkit.getServer()).getHandle().getServer().getServerConnection();
            List<ChannelFuture> futures = (List<ChannelFuture>) channelsField.get(con);
            synchronized (con) {
                TheUnsafe.get().putObject(
                        con,
                        TheUnsafe.get().objectFieldOffset(channelsField),
                        new ProxyList<>(futures, Packets::inject, (ch) -> {})
                );
            }

            String findConnectionsField = List.class.getCanonicalName() + "<" + NetworkManager.class.getCanonicalName() + ">";
            Field connectionsField = Arrays.stream(ServerConnection.class.getDeclaredFields()).filter((f) -> f.getGenericType().getTypeName().equalsIgnoreCase(findConnectionsField)).findAny().get();
            connectionsField.setAccessible(true);
            synchronized (con) {
                List<NetworkManager> connections = (List<NetworkManager>) connectionsField.get(con);
                TheUnsafe.get().putObject(
                        con,
                        TheUnsafe.get().objectFieldOffset(connectionsField),
                        new ProxyList<>(connections, (c) -> Client.getFromChannel(c.channel).setConnection(c), (c) -> {})
                );
            }

            String findServerPlayersField = List.class.getCanonicalName() + "<" + EntityPlayer.class.getCanonicalName() + ">";
            Field serverPlayersField = Arrays.stream(DedicatedPlayerList.class.getDeclaredFields()).filter(f -> f.getGenericType().getTypeName().equalsIgnoreCase(findServerPlayersField)).findAny().get();
            serverPlayersField.setAccessible(true);
            DedicatedPlayerList playerList = ((CraftServer)Bukkit.getServer()).getHandle();
            List<EntityPlayer> players = playerList.players;
            synchronized (((CraftServer)Bukkit.getServer()).getHandle()) {
                TheUnsafe.get().putObject(
                        playerList,
                        TheUnsafe.get().objectFieldOffset(serverPlayersField),
                        new ProxyList<>(players,
                                (p) -> Client.getFromConnection(p.playerConnection.networkManager).setPlayer(p.getBukkitEntity().getPlayer()),
                                (p) -> {})
                );
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(plugin);
        }
    }
}
