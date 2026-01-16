package com.plasma.core.tpa;

import com.plasma.core.PlasmaCore;
import com.plasma.core.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TPAManager {

    private final PlasmaCore plugin;
    private final Map<UUID, UUID> requests = new HashMap<>(); // target -> requester
    private final Map<UUID, BukkitRunnable> expireTasks = new HashMap<>();

    public TPAManager(PlasmaCore plugin) {
        this.plugin = plugin;
    }

    public void sendRequest(Player requester, Player target) {
        if (requester.equals(target)) {
            MessageUtils.send(requester, "tpa-self");
            return;
        }

        UUID requesterUUID = requester.getUniqueId();
        UUID targetUUID = target.getUniqueId();

        // Отменяем старый запрос
        if (expireTasks.containsKey(targetUUID)) {
            expireTasks.get(targetUUID).cancel();
        }

        requests.put(targetUUID, requesterUUID);

        MessageUtils.send(requester, "tpa-sent", "player", target.getName());
        MessageUtils.send(target, "tpa-received", "player", requester.getName());

        // Таймер истечения
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (requests.containsKey(targetUUID) && requests.get(targetUUID).equals(requesterUUID)) {
                    requests.remove(targetUUID);
                    
                    Player r = Bukkit.getPlayer(requesterUUID);
                    if (r != null) {
                        MessageUtils.send(r, "tpa-expired");
                    }
                }
                expireTasks.remove(targetUUID);
            }
        };

        expireTasks.put(targetUUID, task);
        task.runTaskLater(plugin, 60 * 20L); // 60 секунд
    }

    public void acceptRequest(Player target) {
        UUID targetUUID = target.getUniqueId();

        if (!requests.containsKey(targetUUID)) {
            MessageUtils.send(target, "tpa-no-request");
            return;
        }

        UUID requesterUUID = requests.remove(targetUUID);
        
        if (expireTasks.containsKey(targetUUID)) {
            expireTasks.remove(targetUUID).cancel();
        }

        Player requester = Bukkit.getPlayer(requesterUUID);
        if (requester == null || !requester.isOnline()) {
            MessageUtils.send(target, "player-not-found");
            return;
        }

        requester.teleport(target.getLocation());
        MessageUtils.send(target, "tpa-accepted");
        MessageUtils.send(requester, "tpa-accepted");
    }

    public void denyRequest(Player target) {
        UUID targetUUID = target.getUniqueId();

        if (!requests.containsKey(targetUUID)) {
            MessageUtils.send(target, "tpa-no-request");
            return;
        }

        UUID requesterUUID = requests.remove(targetUUID);
        
        if (expireTasks.containsKey(targetUUID)) {
            expireTasks.remove(targetUUID).cancel();
        }

        Player requester = Bukkit.getPlayer(requesterUUID);
        if (requester != null) {
            MessageUtils.send(requester, "tpa-denied");
        }
        
        MessageUtils.send(target, "tpa-denied");
    }
}
