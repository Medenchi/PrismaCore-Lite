package com.plasma.core.homes;

import com.plasma.core.PlasmaCore;
import com.plasma.core.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class HomesManager {

    private final PlasmaCore plugin;
    private final Map<UUID, BukkitRunnable> pendingTeleports = new HashMap<>();

    public HomesManager(PlasmaCore plugin) {
        this.plugin = plugin;
    }

    public void setHome(Player player, String name) {
        UUID uuid = player.getUniqueId();
        int max = plugin.getConfig().getInt("homes.max-homes", 3);
        int count = plugin.getDatabase().getHomesCount(uuid);

        // Проверяем существует ли уже такой дом
        try {
            ResultSet rs = plugin.getDatabase().getHome(uuid, name);
            boolean exists = rs != null && rs.next();
            if (rs != null) rs.close();

            if (!exists && count >= max) {
                MessageUtils.send(player, "home-limit", "max", String.valueOf(max));
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Location loc = player.getLocation();
        plugin.getDatabase().setHome(
            uuid, name,
            loc.getWorld().getName(),
            loc.getX(), loc.getY(), loc.getZ(),
            loc.getYaw(), loc.getPitch()
        );

        MessageUtils.send(player, "home-set", "name", name);
    }

    public void deleteHome(Player player, String name) {
        UUID uuid = player.getUniqueId();

        try {
            ResultSet rs = plugin.getDatabase().getHome(uuid, name);
            if (rs == null || !rs.next()) {
                MessageUtils.send(player, "home-not-found", "name", name);
                if (rs != null) rs.close();
                return;
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        plugin.getDatabase().deleteHome(uuid, name);
        MessageUtils.send(player, "home-deleted", "name", name);
    }

    public void teleportHome(Player player, String name) {
        UUID uuid = player.getUniqueId();

        try {
            ResultSet rs = plugin.getDatabase().getHome(uuid, name);
            if (rs == null || !rs.next()) {
                MessageUtils.send(player, "home-not-found", "name", name);
                if (rs != null) rs.close();
                return;
            }

            String worldName = rs.getString("world");
            double x = rs.getDouble("x");
            double y = rs.getDouble("y");
            double z = rs.getDouble("z");
            float yaw = rs.getFloat("yaw");
            float pitch = rs.getFloat("pitch");
            rs.close();

            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                MessageUtils.sendRaw(player, "&cМир не найден!");
                return;
            }

            Location loc = new Location(world, x, y, z, yaw, pitch);
            int delay = plugin.getConfig().getInt("homes.teleport-delay", 3);

            // Отменяем предыдущий телепорт
            if (pendingTeleports.containsKey(uuid)) {
                pendingTeleports.get(uuid).cancel();
            }

            if (delay <= 0) {
                player.teleport(loc);
                MessageUtils.send(player, "home-teleported");
                return;
            }

            MessageUtils.send(player, "home-teleport", "time", String.valueOf(delay));

            BukkitRunnable task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isOnline()) {
                        player.teleport(loc);
                        MessageUtils.send(player, "home-teleported");
                    }
                    pendingTeleports.remove(uuid);
                }
            };

            pendingTeleports.put(uuid, task);
            task.runTaskLater(plugin, delay * 20L);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<String> getHomes(Player player) {
        List<String> homes = new ArrayList<>();
        try {
            ResultSet rs = plugin.getDatabase().getHomes(player.getUniqueId());
            if (rs != null) {
                while (rs.next()) {
                    homes.add(rs.getString("name"));
                }
                rs.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return homes;
    }

    public void teleportSpawn(Player player) {
        try {
            ResultSet rs = plugin.getDatabase().getSpawn();
            if (rs == null || !rs.next()) {
                MessageUtils.sendRaw(player, "&cСпавн не установлен!");
                if (rs != null) rs.close();
                return;
            }

            String worldName = rs.getString("world");
            double x = rs.getDouble("x");
            double y = rs.getDouble("y");
            double z = rs.getDouble("z");
            float yaw = rs.getFloat("yaw");
            float pitch = rs.getFloat("pitch");
            rs.close();

            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                MessageUtils.sendRaw(player, "&cМир не найден!");
                return;
            }

            Location loc = new Location(world, x, y, z, yaw, pitch);
            MessageUtils.send(player, "spawn-teleport");
            player.teleport(loc);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setSpawn(Player player) {
        Location loc = player.getLocation();
        plugin.getDatabase().setSpawn(
            loc.getWorld().getName(),
            loc.getX(), loc.getY(), loc.getZ(),
            loc.getYaw(), loc.getPitch()
        );
        MessageUtils.send(player, "spawn-set");
    }
}
