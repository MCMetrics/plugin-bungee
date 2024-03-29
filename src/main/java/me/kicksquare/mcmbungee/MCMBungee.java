package me.kicksquare.mcmbungee;

import de.leonhard.storage.Config;
import de.leonhard.storage.SimplixBuilder;
import de.leonhard.storage.internal.settings.DataType;
import de.leonhard.storage.internal.settings.ReloadSettings;
import me.kicksquare.mcmbungee.commands.MCMCommand;
import me.kicksquare.mcmbungee.commands.PaymentCommand;
import me.kicksquare.mcmbungee.listeners.GlobalBansListener;
import me.kicksquare.mcmbungee.util.HttpUtil;
import me.kicksquare.mcmbungee.util.LoggerUtil;
import me.kicksquare.mcmbungee.util.Metrics;
import me.kicksquare.mcmbungee.util.SetupUtil;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;

public final class MCMBungee extends Plugin {

    private static MCMBungee plugin;

    private Config mainConfig;
    private Config dataConfig;
    private Config bansConfig;

    @Override
    public void onEnable() {
        plugin = this;

        // setup config
        mainConfig = SimplixBuilder
                .fromFile(new File(getDataFolder(), "config.yml"))
                .addInputStreamFromResource("config.yml")
                .setDataType(DataType.SORTED)
                .setReloadSettings(ReloadSettings.MANUALLY)
                .createConfig();

        dataConfig = SimplixBuilder
                .fromFile(new File(getDataFolder(), "data/data.yml"))
                .addInputStreamFromResource("data.yml")
                .setDataType(DataType.SORTED)
                .setReloadSettings(ReloadSettings.MANUALLY)
                .createConfig();

        bansConfig = SimplixBuilder
                .fromFile(new File(getDataFolder(), "globalbans.yml"))
                .addInputStreamFromResource("globalbans.yml")
                .setDataType(DataType.SORTED)
                .setReloadSettings(ReloadSettings.MANUALLY)
                .createConfig();

        MCMCommand.reloadConfigAndFetchData();

        ProxyServer.getInstance().getPluginManager().registerCommand(this, new MCMCommand());
        ProxyServer.getInstance().getPluginManager().registerCommand(this, new PaymentCommand());
        ProxyServer.getInstance().getPluginManager().registerListener(this, new GlobalBansListener(this, getProxy()));


        // enable bstats
        if (mainConfig.getBoolean("enable-bstats")) {
            new Metrics(this, 17891);
        }

        // every 5 minutes, log "hello world" to console
        getProxy().getScheduler().schedule(this, this::uploadPlayerCount, 0, dataConfig.getInt("ping-interval"), java.util.concurrent.TimeUnit.MINUTES);
    }

    public void uploadPlayerCount() {
        if (!SetupUtil.shouldRecordPings()) return;
        if (dataConfig.getInt("ping-interval") == 0) return;

        try {
            LoggerUtil.debug("Sending playercount ping");
            final String bodyString = "{\"playercount\": \"" + getProxy().getPlayers().size() + "\"}";
            HttpUtil.makeAsyncPostRequest("https://dashboard.mcmetrics.net/api/pings/insertPing", bodyString, HttpUtil.getAuthHeadersFromConfig());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static MCMBungee getPlugin() {
        return plugin;
    }

    public Config getMainConfig() {
        return mainConfig;
    }

    public Config getDataConfig() {
        return dataConfig;
    }

    public Config getBansConfig() {
        return bansConfig;
    }
}
