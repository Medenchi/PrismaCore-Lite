package com.plasma.core.tab;

import com.plasma.core.PlasmaCore;
import com.plasma.core.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class TabManager implements Listener {

    private final PlasmaCore plugin;

    public TabManager(PlasmaCore plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        startUpdateTask();
    }

    private void startUpdateTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    updateTab(player);
                }
            }
        }.runTaskTimer(plugin, 40L, 40L); // Каждые 2 секунды
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> updateTab(event.getPlayer()), 20L);
    }

    public void updateTab(Player player) {
        String header = plugin.getConfig().getString("tab.header", "\n&b&lPLASMA SERVER\n");
        String footer = plugin.getConfig().getString("tab.footer", "\n&7Онлайн: &a{online}&7/&a{max}\n");

        int online = Bukkit.getOnlinePlayers().size();
        int max = Bukkit.getMaxPlayers();
        int ping = player.getPing();

        header = header
            .replace("{online}", String.valueOf(online))
            .replace("{max}", String.valueOf(max))
            .replace("{ping}", String.valueOf(ping))
            .replace("{player}", player.getName());

        footer = footer
            .replace("{online}", String.valueOf(online))
            .replace("{max}", String.valueOf(max))
            .replace("{ping}", String.valueOf(ping))
            .replace("{player}", player.getName());

        player.sendPlayerListHeaderAndFooter(
            MessageUtils.color(header),
            MessageUtils.color(footer)
        );
    }
}
