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

        // mcmpayment <tebex|craftingstore> <username> <transaction_id> <amount> <currency>
        if (args.length != 5) {
            sender.sendMessage("Usage: /mcmpayment <tebex|craftingstore> <username> <transaction_id> <amount> <currency>");
            return;
        }

        final String platform = args[0];
        final String username = args[1];
        final String transaction_id = args[2];
        String amount = args[3];
        final String currency = args[4];

        // transaction fee option from config
        double amountDouble = Double.parseDouble(amount);
        final double paymentFeeOption = plugin.getMainConfig().getDouble("payment-fee");
        if (paymentFeeOption > 0) {
            amountDouble = amountDouble * (1 - paymentFeeOption);
            amount = String.valueOf(amountDouble);
        }

        // validate platform
        if (!platform.equalsIgnoreCase("tebex") && !platform.equalsIgnoreCase("craftingstore")) {
            sender.sendMessage("Invalid platform. Must be either 'tebex' or 'craftingstore'.");
            return;
        }

        PlayerPayment playerPayment = new PlayerPayment(plugin, platform, username, transaction_id, amount, currency);

        // get the payment as a json string
        String jsonString;
        try {
            jsonString = mapper.writeValueAsString(playerPayment);
        } catch (JsonProcessingException ex) {
            LoggerUtil.severe("Error converting incoming payment to json string.");
            throw new RuntimeException(ex);
        }

        LoggerUtil.debug("Uploading payment session now... " + jsonString);

        HttpUtil.makeAsyncPostRequest("https://dashboard.mcmetrics.net/api/payments/insertUsernamePayment", jsonString, HttpUtil.getAuthHeadersFromConfig());

        return;
    }
}