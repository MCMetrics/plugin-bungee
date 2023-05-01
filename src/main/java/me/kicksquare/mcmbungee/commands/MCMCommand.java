package me.kicksquare.mcmbungee.commands;

import me.kicksquare.mcmbungee.MCMBungee;
import me.kicksquare.mcmbungee.types.TaskList;
import me.kicksquare.mcmbungee.util.HttpUtil;
import me.kicksquare.mcmbungee.util.SetupUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.concurrent.CompletableFuture;

public class MCMCommand extends Command {
    private static MCMBungee plugin = MCMBungee.getPlugin();

    public MCMCommand() {
        super("mcmetrics", "mcmetrics.command", "mcm", "/mcmetrics", "/mcm");
    }

    public static CompletableFuture<Boolean> reloadConfigAndFetchData() {
        return CompletableFuture.supplyAsync(() -> {
            plugin.getMainConfig().forceReload();
            plugin.getDataConfig().forceReload();
            plugin.getBansConfig().forceReload();

            if (SetupUtil.isSetup()) {
                TaskList.fetchTasks();
            } else {
                plugin.getLogger().warning("Reloaded plugin, but the plugin is not configured! Please run /mcmetrics setup <user id> <server id> to configure the plugin.");
            }

            return true;
        });
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // permission check
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            if (!player.hasPermission("mcmetrics.commands")) {
                player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                return;
            }
        }

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                reloadConfigAndFetchData().thenAccept((result) -> {
                    if (result) {
                        plugin.uploadPlayerCount(); // manually force upload player count
                        sender.sendMessage(ChatColor.GREEN + "Successfully reloaded the config!");
                        if (plugin.getMainConfig().getBoolean("enable-sentry")) return; // already enabled
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Optional Sentry Opt-In: Run &9'mcmetrics enablesentry'&7 to enable anonymous error-reporting via Sentry (you can change this later in the config)."));

                    } else {
                        sender.sendMessage(ChatColor.RED + "Failed to reload the config!");
                    }
                });
                return;
            } else if (args[0].equalsIgnoreCase("enablesentry")) {
                plugin.getMainConfig().set("enable-sentry", true);
                sender.sendMessage(ChatColor.GREEN + "Successfully enabled Sentry!");
                return;
            } else if (args[0].equalsIgnoreCase("setup")) {
                sender.sendMessage(ChatColor.RED + "Usage: /mcmetrics setup <user id> <server id>");
                return;
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("setup")) {
                final String uid = args[1];
                final String serverId = args[2];

                if (uid.length() != 25) {
                    sender.sendMessage(ChatColor.RED + "Invalid user id! User ID must be 25 characters long!");
                    return;
                } else if (serverId.length() != 36) {
                    sender.sendMessage(ChatColor.RED + "Invalid server id! Server ID must be 36 characters long!");
                    return;
                }

                // set config key "uid" to uid, and "server_id" to serverId, and "setup-complete" to true
                plugin.getMainConfig().set("uid", uid);
                plugin.getMainConfig().set("server_id", serverId);
                plugin.getDataConfig().set("setup-complete", true);
                reloadConfigAndFetchData();

                // set server connected to true
                HttpUtil.makeAsyncGetRequest("https://dashboard.mcmetrics.net/api/server/setServerIsSetup", HttpUtil.getAuthHeadersFromConfig());

                sender.sendMessage(ChatColor.GREEN + "Successfully configured the plugin!");
                return;
            }
        }

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e&lMCMetrics"));
        sender.sendMessage(ChatColor.GRAY + "Plugin Commands:");
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7 • &9/mcmetrics reload&7 - Reloads the config"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7 • &9/mcmetrics setup <user id> <server id>&7 - Automatically configures the plugin"));

    }
}
