package com.plasma.core.commands;

import com.plasma.core.PlasmaCore;
import com.plasma.core.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BalanceCommand implements CommandExecutor {

    private final PlasmaCore plugin;

    public BalanceCommand(PlasmaCore plugin) {
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

        if (args.length > 0 && player.hasPermission("plasma.admin")) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                MessageUtils.send(player, "player-not-found");
                return true;
            }
            
            double balance = plugin.getCoinsManager().getBalance(target);
            MessageUtils.send(player, "balance-other", 
                "player", target.getName(),
                "balance", String.format("%.0f", balance)
            );
            return true;
        }

        double balance = plugin.getCoinsManager().getBalance(player);
        MessageUtils.send(player, "balance", "balance", String.format("%.0f", balance));
        return true;
    }
              }
