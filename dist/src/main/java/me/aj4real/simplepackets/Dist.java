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

public class Dist {
    private static Plugin plugin;
    private static SimplePackets nms;
    public static SimplePackets init(Plugin plugin) {
        String regex = "\\d+(\\.\\d+)+";
        String strVer = Bukkit.getVersion();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(strVer);
        matcher.find();
        strVer = 'v' + matcher.group();
        try {
            plugin.getLogger().log(Level.INFO, Dist.class.getCanonicalName() + ": Attempting to load NMS interface for " + strVer);
            Version ver = Version.valueOf(strVer.replace('.', '_'));
            Dist.nms = ver.nms.newInstance();
            Dist.nms.onEnable(plugin);
            Dist.plugin = plugin;
            Client.nms = nms;
            Packets.onEnable(plugin);
            return nms;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, Dist.class.getCanonicalName() + ": Could not initiate support for " + strVer + ", Is it a supported version?", e);
            return null;
        }
    }
    public enum Version {
        v1_17(me.aj4real.simplepackets.nms.v1_17.SimplePacketsImpl.class),
        v1_17_1(SimplePacketsImpl.class),
        v1_18(me.aj4real.simplepackets.nms.v1_18.SimplePacketsImpl.class),
        v1_18_1(me.aj4real.simplepackets.nms.v1_18_1.SimplePacketsImpl.class),
        v1_18_2(me.aj4real.simplepackets.nms.v1_18_2.SimplePacketsImpl.class),
        v1_19(me.aj4real.simplepackets.nms.v1_19.SimplePacketsImpl.class),
        v1_19_1(me.aj4real.simplepackets.nms.v1_19_1.SimplePacketsImpl.class),
        v1_19_2(me.aj4real.simplepackets.nms.v1_19_2.SimplePacketsImpl.class),
        v1_19_3(me.aj4real.simplepackets.nms.v1_19_3.SimplePacketsImpl.class),
        v1_19_4(me.aj4real.simplepackets.nms.v1_19_4.SimplePacketsImpl.class);
        private final Class<? extends SimplePackets> nms;
        Version(Class<? extends SimplePackets> nms) {
            this.nms = nms;
        }
    }
}
