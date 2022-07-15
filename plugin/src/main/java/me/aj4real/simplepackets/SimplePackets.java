/**********************************
 Copyright (c) All Rights Reserved
 *********************************/

package me.aj4real.simplepackets;

import org.bukkit.plugin.Plugin;

public interface SimplePackets {

    void onEnable(Plugin plugin);
    default void onDisable(Plugin plugin) {
        Packets.onDisable(plugin);
    }
}
