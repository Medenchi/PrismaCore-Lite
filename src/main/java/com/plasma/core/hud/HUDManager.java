package com.plasma.core.hud;

import com.plasma.core.PlasmaCore;
import com.plasma.core.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class HUDManager {

    private final PlasmaCore plugin;

    public HUDManager(PlasmaCore plugin) {
        this.plugin = plugin;
        startUpdateTask();
    }

    private void startUpdateTask() {
        int ticks = plugin.getConfig().getInt("hud.update-ticks", 20);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (plugin.getAuthManager().isLoggedIn(player)) {
                        updateHUD(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 40L, ticks);
    }

    public void updateHUD(Player player) {
        String format = plugin.getConfig().getString("hud.format", "&c❤ {health} &7| &e{coins}⛃");

        double health = player.getHealth();
        double coins = plugin.getCoinsManager().getBalance(player);
        int online = Bukkit.getOnlinePlayers().size();

        String hud = format
            .replace("{health}", String.format("%.0f", health))
            .replace("{coins}", String.format("%.0f", coins))
            .replace("{players}", String.valueOf(online))
            .replace("{online}", String.valueOf(online));

        player.sendActionBar(MessageUtils.color(hud));
    }
}
