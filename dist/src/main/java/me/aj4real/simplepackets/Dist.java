/**********************************
 Copyright (c) All Rights Reserved
 *********************************/

package me.aj4real.simplepackets;

import me.aj4real.simplepackets.nms.v1_17.SimplePacketsImpl;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        String regex = "\\d+(\\.\\d+)+";
        String strVer = Bukkit.getVersion();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(strVer);
        matcher.find();
        strVer = 'v' + matcher.group();
        try {
            plugin.getLogger().log(Level.INFO, "Attempting to load NMS interface for " + strVer);
            Version ver = Version.valueOf(strVer.replace('.', '_'));
            Packets.identifier = plugin.getName().toLowerCase();
            SimplePackets nms = ver.nms.newInstance();
            nms.onEnable(plugin);
            return nms;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, Dist.class.getCanonicalName() + ": Could not initiate support for " + strVer + ", Is it a supported version?", e);
            return null;
        }
    }
    public void onDisable() {
        main.onDisable(this);
    }
    public enum Version {
        v1_17(me.aj4real.simplepackets.nms.v1_17.SimplePacketsImpl.class),
        v1_17_1(SimplePacketsImpl.class),
        v1_18(me.aj4real.simplepackets.nms.v1_18.SimplePacketsImpl.class),
        v1_18_1(me.aj4real.simplepackets.nms.v1_18_1.SimplePacketsImpl.class),
        v1_18_2(me.aj4real.simplepackets.nms.v1_18_2.SimplePacketsImpl.class),
        v1_19(me.aj4real.simplepackets.nms.v1_19.SimplePacketsImpl.class),
        v1_19_1(me.aj4real.simplepackets.nms.v1_19_1.SimplePacketsImpl.class),
        v1_19_2(me.aj4real.simplepackets.nms.v1_19_2.SimplePacketsImpl.class);
        private final Class<? extends SimplePackets> nms;
        Version(Class<? extends SimplePackets> nms) {
            this.nms = nms;
        }
    }
}
