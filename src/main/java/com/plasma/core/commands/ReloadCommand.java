package com.plasma.core.commands;

import com.plasma.core.PlasmaCore;
import com.plasma.core.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReloadCommand implements CommandExecutor {

    private final PlasmaCore plugin;

    public ReloadCommand(PlasmaCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player && !player.hasPermission("plasma.admin")) {
            MessageUtils.send(player, "no-permission");
            return true;
        }

        plugin.reloadConfig();
        sender.sendMessage("§a§lPlasmaCore §8» §aКонфиг перезагружен!");
        return true;
    }
}
