package me.kicksquare.mcmbungee.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.kicksquare.mcmbungee.MCMBungee;
import me.kicksquare.mcmbungee.types.PlayerPayment;
import me.kicksquare.mcmbungee.util.HttpUtil;
import me.kicksquare.mcmbungee.util.LoggerUtil;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;


public class PaymentCommand extends Command {

    private static final ObjectMapper mapper = new ObjectMapper();

    private static final MCMBungee plugin = MCMBungee.getPlugin();

    public PaymentCommand() {
        super("mcmpayment");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            sender.sendMessage("This command can only be run from the console.");
            return;
        }

        // mcmpayment <tebex|craftingstore> <player_uuid> <transaction_id> <amount> <currency> <package_id>
        if (args.length != 6) {
            sender.sendMessage("Usage: /mcmpayment <tebex|craftingstore> <player_uuid> <transaction_id> <amount> <currency> <package_id>");
            return;
        }

        final String platform = args[0];
        final String player_uuid = args[1];
        final String transaction_id = args[2];
        final String amount = args[3];
        final String currency = args[4];
        final String package_id = args[5];

        if (!platform.equalsIgnoreCase("tebex") && !platform.equalsIgnoreCase("craftingstore")) {
            sender.sendMessage("Invalid platform. Must be either 'tebex' or 'craftingstore'.");
            return;
        }

        PlayerPayment playerPayment = new PlayerPayment(plugin, platform, player_uuid, transaction_id, amount, currency, package_id);

        // get the payment as a json string
        String jsonString;
        try {
            jsonString = mapper.writeValueAsString(playerPayment);
        } catch (JsonProcessingException ex) {
            LoggerUtil.severe("Error converting incoming payment to json string.");
            throw new RuntimeException(ex);
        }

        LoggerUtil.debug("Uploading payment session now... " + jsonString);

        HttpUtil.makeAsyncPostRequest("api/payments/insertPayment", jsonString, HttpUtil.getAuthHeadersFromConfig());

        return;
    }
}