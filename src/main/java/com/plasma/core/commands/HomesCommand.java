package com.plasma.core.commands;

import com.plasma.core.PlasmaCore;
import com.plasma.core.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class HomesCommand implements CommandExecutor {

    private final PlasmaCore plugin;

    public HomesCommand(PlasmaCore plugin) {
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

        List<String> homes = plugin.getHomesManager().getHomes(player);

        if (homes.isEmpty()) {
            MessageUtils.sendRaw(player, "&eУ вас нет домов. Используйте &a/sethome <название>");
            return true;
        }

        int max = plugin.getConfig().getInt("homes.max-homes", 3);
        MessageUtils.sendRaw(player, "&b&lВаши дома &7(" + homes.size() + "/" + max + ")&b:");
        
        for (String home : homes) {
            MessageUtils.sendRaw(player, "&8 • &a" + home + " &7(/home " + home + ")");
        }

        return true;
    }
                                 }
