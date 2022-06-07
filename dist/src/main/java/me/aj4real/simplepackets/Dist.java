/**********************************
 Copyright (c) All Rights Reserved
 *********************************/

package me.aj4real.simplepackets;

import org.bukkit.Bukkit;
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
        main.onEnable(this, getNMS(this)); // For the sake of shading.
    }
    public static NMS getNMS(Plugin plugin) {
        String s = Arrays.stream(Package.getPackages())
                .map(Package::getName)
                .filter(n -> n.startsWith("org.bukkit.craftbukkit.v1_"))
                .collect(Collectors.toList()).stream().findFirst().get()
                .replace("org.bukkit.craftbukkit.", "").split("\\.")[0];
        try {
            plugin.getLogger().log(Level.INFO, "Attempting to load NMS interface for " + s);
            NMS nms = Version.valueOf(s).nms.newInstance();
            nms.onEnable(plugin);
            return nms;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Could not initiate support for " + s + ", Is it a supported version?", e);
            Bukkit.getPluginManager().disablePlugin(plugin);
            return null;
        }
    }
    public void onDisable() {
        main.onDisable(this);
    }
    public enum Version {
        v1_12_R1(me.aj4real.simplepackets.nms.v1_12_R1.NMSImpl.class),
        v1_13_R1(me.aj4real.simplepackets.nms.v1_13_R1.NMSImpl.class),
        v1_13_R2(me.aj4real.simplepackets.nms.v1_13_R2.NMSImpl.class),
        v1_14_R1(me.aj4real.simplepackets.nms.v1_14_R1.NMSImpl.class),
        v1_15_R1(me.aj4real.simplepackets.nms.v1_15_R1.NMSImpl.class),
        v1_16_R1(me.aj4real.simplepackets.nms.v1_16_R1.NMSImpl.class),
        v1_16_R2(me.aj4real.simplepackets.nms.v1_16_R2.NMSImpl.class),
        v1_16_R3(me.aj4real.simplepackets.nms.v1_16_R3.NMSImpl.class),
        v1_17_R1(me.aj4real.simplepackets.nms.v1_17_R1.NMSImpl.class),
        v1_18_R1(me.aj4real.simplepackets.nms.v1_18_R1.NMSImpl.class),
        v1_18_R2(me.aj4real.simplepackets.nms.v1_18_R2.NMSImpl.class);
        private final Class<? extends NMS> nms;
        Version(Class<? extends NMS> nms) {
            this.nms = nms;
        }
    }
}
