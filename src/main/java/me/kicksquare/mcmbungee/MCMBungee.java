package me.kicksquare.mcmbungee;

import de.leonhard.storage.Config;
import de.leonhard.storage.SimplixBuilder;
import de.leonhard.storage.internal.settings.DataType;
import de.leonhard.storage.internal.settings.ReloadSettings;
import io.sentry.Sentry;
import me.kicksquare.mcmbungee.commands.MCMCommand;
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

        MCMCommand.reloadConfigAndFetchData();

        ProxyServer.getInstance().getPluginManager().registerCommand(this, new MCMCommand());

        // enable bstats
        if (mainConfig.getBoolean("enable-bstats")) {
            new Metrics(this, 17891);
        }

        // enable sentry error reporting
        if (mainConfig.getBoolean("enable-sentry")) {
            Sentry.init(options -> {
                options.setDsn("https://d9b2edffd9564e7e89f40663d093567f@o4504532201046017.ingest.sentry.io/4504800584794112");
                options.setTracesSampleRate(0.1);
                options.setDebug(false);
            });

            // checks for exceptions matching this plugin name and uploads them to sentry
            Thread.setDefaultUncaughtExceptionHandler(new SentryExceptionHandler());
        }

        // every 5 minutes, log "hello world" to console
        getProxy().getScheduler().schedule(this, () -> {
            if (!SetupUtil.shouldRecordPings()) return;
            if(dataConfig.getInt("ping-interval") == 0) return;

            try {
                LoggerUtil.debug("Sending playercount ping");
                final String bodyString = "{\"playercount\": \"" + getProxy().getPlayers().size() + "\"}";
                HttpUtil.makeAsyncPostRequest("https://dashboard.mcmetrics.net/api/pings/insertPing", bodyString, HttpUtil.getAuthHeadersFromConfig());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, dataConfig.getInt("ping-interval"), java.util.concurrent.TimeUnit.MINUTES);
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
}
