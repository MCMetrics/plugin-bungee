package me.kicksquare.mcmbungee.util;

import me.kicksquare.mcmbungee.MCMBungee;

public class LoggerUtil {

    private static final MCMBungee plugin = MCMBungee.getPlugin();

    public static void warning(String message) {
        plugin.getLogger().warning(message);
    }

    public static void info(String message) {
        plugin.getLogger().info(message);
    }

    public static void debug(String message) {
        if (plugin.getMainConfig().getBoolean("debug")) {
            plugin.getLogger().info("[DEBUG] " + message);
        }
    }

    public static void severe(String message) {
        plugin.getLogger().severe(message);
    }

}