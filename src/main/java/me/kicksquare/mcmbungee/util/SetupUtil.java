package me.kicksquare.mcmbungee.util;

import de.leonhard.storage.Config;
import me.kicksquare.mcmbungee.MCMBungee;

public class SetupUtil {

    private static MCMBungee plugin = MCMBungee.getPlugin();
    private static Config config = plugin.getMainConfig();
    private static Config dataConfig = plugin.getDataConfig();
    private static Config bansConfig = plugin.getBansConfig();

    public static boolean isSetup() {
        return dataConfig.getBoolean("setup-complete") &&
                !(config.getString("server_id").equals("") || config.getString("uid") == "");
    }

    public static boolean shouldRecordPings() {
        Config dataConfig = plugin.getDataConfig();
        return isSetup() && dataConfig.getBoolean("record-pings");
    }

    public static boolean shouldCheckGlobalBans() {
        return isSetup() && dataConfig.getBoolean("global-bans") && bansConfig.getBoolean("enabled");
    }
}
