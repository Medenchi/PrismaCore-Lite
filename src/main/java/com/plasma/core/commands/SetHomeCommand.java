package com.plasma.core.commands;

import com.plasma.core.PlasmaCore;
import com.plasma.core.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetHomeCommand implements CommandExecutor {

    private final PlasmaCore plugin;

    public SetHomeCommand(PlasmaCore plugin) {
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

        String homeName = args.length > 0 ? args[0] : "home";
        plugin.getHomesManager().setHome(player, homeName);
        return true;
    }
}
