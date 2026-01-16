package com.plasma.core.commands;

import com.plasma.core.PlasmaCore;
import com.plasma.core.utils.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LoginCommand implements CommandExecutor {

    private final PlasmaCore plugin;

    public LoginCommand(PlasmaCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cТолько для игроков!");
            return true;
        }

        if (plugin.getAuthManager().isLoggedIn(player)) {
            MessageUtils.sendRaw(player, "&aВы уже авторизованы!");
            return true;
        }

        if (!plugin.getDatabase().isRegistered(player.getUniqueId())) {
            MessageUtils.send(player, "not-registered");
            return true;
        }

        if (args.length < 1) {
            MessageUtils.sendRaw(player, "&cИспользование: /login <пароль>");
            return true;
        }

        String password = args[0];
        String hash = plugin.getAuthManager().hashPassword(password);
        String stored = plugin.getDatabase().getPassword(player.getUniqueId());

        if (!hash.equals(stored)) {
            MessageUtils.send(player, "wrong-password");
            return true;
        }

        plugin.getAuthManager().login(player);
        MessageUtils.send(player, "login-success");
        return true;
    }
}
