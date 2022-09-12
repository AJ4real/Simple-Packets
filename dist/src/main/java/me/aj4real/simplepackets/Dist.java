/**********************************
 Copyright (c) All Rights Reserved
 *********************************/

package me.aj4real.simplepackets;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class Dist extends JavaPlugin {
    Dist plugin = null;
    Main main = new Main();
    public void onLoad() {
        this.plugin = this;
        main.onLoad(this);
    }
    public void onEnable() {
        main.onEnable(this, init(this)); // For the sake of shading.
    }
    public static SimplePackets init(Plugin plugin) {
        String s = Arrays.stream(Package.getPackages())
                .map(Package::getName)
                .filter(n -> n.startsWith("org.bukkit.craftbukkit.v1_"))
                .collect(Collectors.toList()).stream().findFirst().get()
                .replace("org.bukkit.craftbukkit.", "").split("\\.")[0];
        try {
            plugin.getLogger().log(Level.INFO, Dist.class.getCanonicalName() + ": Attempting to load NMS interface for " + s);
            Packets.identifier = plugin.getName().toLowerCase();
            SimplePackets nms = Version.valueOf(s).nms.newInstance();
            nms.onEnable(plugin);
            return nms;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, Dist.class.getCanonicalName() + ": Could not initiate support for " + s + ", Is it a supported version?", e);
            return null;
        }
    }
    public void onDisable() {
        main.onDisable(this);
    }
    public enum Version {
        v1_17_R1(me.aj4real.simplepackets.nms.v1_17_R1.SimplePacketsImpl.class),
        v1_18_R1(me.aj4real.simplepackets.nms.v1_18_R1.SimplePacketsImpl.class),
        v1_18_R2(me.aj4real.simplepackets.nms.v1_18_R2.SimplePacketsImpl.class),
        v1_19_R1(me.aj4real.simplepackets.nms.v1_19_R1.SimplePacketsImpl.class);
        private final Class<? extends SimplePackets> nms;
        Version(Class<? extends SimplePackets> nms) {
            this.nms = nms;
        }
    }
}
