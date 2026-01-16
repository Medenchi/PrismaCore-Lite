package com.plasma.core.coins;

import com.plasma.core.PlasmaCore;
import com.plasma.core.utils.MessageUtils;
import org.bukkit.entity.Player;

public class CoinsManager {

    private final PlasmaCore plugin;

    public CoinsManager(PlasmaCore plugin) {
        this.plugin = plugin;
    }

    public double getBalance(Player player) {
        return plugin.getDatabase().getCoins(player.getUniqueId());
    }

    public void addCoins(Player player, double amount) {
        plugin.getDatabase().addCoins(player.getUniqueId(), amount);
    }

    public void removeCoins(Player player, double amount) {
        plugin.getDatabase().removeCoins(player.getUniqueId(), amount);
    }

    public void setCoins(Player player, double amount) {
        plugin.getDatabase().setCoins(player.getUniqueId(), amount);
    }

    public boolean hasEnough(Player player, double amount) {
        return getBalance(player) >= amount;
    }

    public void pay(Player from, Player to, double amount) {
        if (!hasEnough(from, amount)) {
            MessageUtils.send(from, "pay-no-money");
            return;
        }

        removeCoins(from, amount);
        addCoins(to, amount);

        String amountStr = String.format("%.0f", amount);
        MessageUtils.send(from, "pay-success", "amount", amountStr, "player", to.getName());
        MessageUtils.send(to, "pay-received", "amount", amountStr, "player", from.getName());
    }

    public String formatBalance(double balance) {
        String symbol = plugin.getConfig().getString("coins.symbol", "⛃");
        return String.format("%.0f%s", balance, symbol);
    }
}
