package com.plasma.core.commands;

import com.plasma.core.PlasmaCore;
import com.plasma.core.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TPACommand implements CommandExecutor {

    private final PlasmaCore plugin;

    public TPACommand(PlasmaCore plugin) {
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

        if (args.length < 1) {
            MessageUtils.sendRaw(player, "&cИспользование: /tpa <игрок>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            MessageUtils.send(player, "player-not-found");
            return true;
        }

        plugin.getTpaManager().sendRequest(player, target);
        return true;
    }
}
