package me.kicksquare.mcmbungee.util;

import de.leonhard.storage.Config;
import me.kicksquare.mcmbungee.MCMBungee;

public class SetupUtil {

    private static MCMBungee plugin = MCMBungee.getPlugin();

    public static boolean isSetup() {
        Config config = plugin.getMainConfig();
        Config dataConfig = plugin.getDataConfig();

        return dataConfig.getBoolean("setup-complete") &&
                !(config.getString("server_id").equals("") || config.getString("uid") == "");
    }

    public static boolean shouldRecordPings() {
        Config config = plugin.getMainConfig();
        Config dataConfig = plugin.getDataConfig();
        return isSetup() && dataConfig.getBoolean("record-pings");
    }
}
