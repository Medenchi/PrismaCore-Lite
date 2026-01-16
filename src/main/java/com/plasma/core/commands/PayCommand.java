package com.plasma.core.commands;

import com.plasma.core.PlasmaCore;
import com.plasma.core.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PayCommand implements CommandExecutor {

    private final PlasmaCore plugin;

    public PayCommand(PlasmaCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cТолько для игроков!");
            return true;
        }

        if (!plugin.getAuthManager().isLoggedIn(player)) {
            MessageUtils.send(player, "login-required");
            return true;
        }

        if (args.length < 2) {
            MessageUtils.sendRaw(player, "&cИспользование: /pay <игрок> <сумма>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            MessageUtils.send(player, "player-not-found");
            return true;
        }

        if (target.equals(player)) {
            MessageUtils.sendRaw(player, "&cНельзя перевести себе!");
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
            if (amount <= 0) {
                MessageUtils.send(player, "pay-invalid");
                return true;
            }
        } catch (NumberFormatException e) {
            MessageUtils.send(player, "pay-invalid");
            return true;
        }

        plugin.getCoinsManager().pay(player, target, amount);
        return true;
    }
}
