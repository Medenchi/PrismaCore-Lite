package com.plasma.core.auth;

import com.plasma.core.PlasmaCore;
import com.plasma.core.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AuthManager implements Listener {

    private final PlasmaCore plugin;
    private final Set<UUID> loggedIn = new HashSet<>();
    private final Set<UUID> frozen = new HashSet<>();

    public AuthManager(PlasmaCore plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public boolean isLoggedIn(Player player) {
        return loggedIn.contains(player.getUniqueId());
    }

    public void login(Player player) {
        UUID uuid = player.getUniqueId();
        loggedIn.add(uuid);
        frozen.remove(uuid);

        // Создаём сессию
        String ip = player.getAddress().getAddress().getHostAddress();
        int minutes = plugin.getConfig().getInt("auth.session-minutes", 60);
        long expires = System.currentTimeMillis() + (minutes * 60 * 1000L);
        plugin.getDatabase().createSession(uuid, ip, expires);

        // Включаем scoreboard и т.д.
        if (plugin.getScoreboardManager() != null) {
            plugin.getScoreboardManager().showScoreboard(player);
        }

        player.setWalkSpeed(0.2f);
        player.setFlySpeed(0.1f);
    }

    public void logout(Player player) {
        loggedIn.remove(player.getUniqueId());
    }

    public String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return password;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Создаём игрока в БД
        plugin.getDatabase().createPlayer(uuid, player.getName());

        // Проверяем сессию
        String ip = player.getAddress().getAddress().getHostAddress();
        if (plugin.getDatabase().hasValidSession(uuid, ip)) {
            loggedIn.add(uuid);
            MessageUtils.sendRaw(player, "&aАвтоматический вход по сессии!");
            
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (plugin.getScoreboardManager() != null) {
                    plugin.getScoreboardManager().showScoreboard(player);
                }
            }, 10L);
            return;
        }

        // Замораживаем
        frozen.add(uuid);
        player.setWalkSpeed(0f);
        player.setFlySpeed(0f);

        // Сообщение
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!isLoggedIn(player)) {
                if (plugin.getDatabase().isRegistered(uuid)) {
                    MessageUtils.send(player, "login-required");
                } else {
                    MessageUtils.send(player, "not-registered");
                }
            }
        }, 20L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        frozen.remove(uuid);
        // Не удаляем из loggedIn — сессия сохраняется
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMove(PlayerMoveEvent event) {
        if (frozen.contains(event.getPlayer().getUniqueId())) {
            Location from = event.getFrom();
            Location to = event.getTo();
            if (from.getX() != to.getX() || from.getZ() != to.getZ()) {
                event.setTo(from);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        if (!isLoggedIn(event.getPlayer())) {
            event.setCancelled(true);
            MessageUtils.send(event.getPlayer(), "login-required");
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (!isLoggedIn(event.getPlayer())) {
            String cmd = event.getMessage().toLowerCase();
            if (!cmd.startsWith("/login") && !cmd.startsWith("/register") && 
                !cmd.startsWith("/l ") && !cmd.startsWith("/reg ")) {
                event.setCancelled(true);
                MessageUtils.send(event.getPlayer(), "login-required");
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!isLoggedIn(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!isLoggedIn(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDrop(PlayerDropItemEvent event) {
        if (!isLoggedIn(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPickup(PlayerAttemptPickupItemEvent event) {
        if (!isLoggedIn(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent event) {
        if (!isLoggedIn(event.getPlayer())) {
            event.setCancelled(true);
        }
    }
}
