package de.febanhd.mlgrush.util;

import de.febanhd.mlgrush.nms.*;
import org.bukkit.Bukkit;

public class ApiversionChecker {

    public static String getVersion(){
        String s = Bukkit.getServer().getBukkitVersion();
        return s.split("-")[0];
    }

    public static NMSBase getNMSBase() {
        String version = getVersion();
        if(version.startsWith("1.16")) {
            return new NMSBase_1_16();
        }else if (version.startsWith("1.15")) {
            return new NMSBase_1_15();
        }else if (version.startsWith("1.12")) {
            return new NMSBase_1_12();
        }else if (version.startsWith("1.8.8")) {
            return new NMSBase_1_8();
        }
        return null;
    }
}

