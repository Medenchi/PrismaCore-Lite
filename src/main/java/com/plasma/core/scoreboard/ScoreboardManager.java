package com.plasma.core.scoreboard;

import com.plasma.core.PlasmaCore;
import com.plasma.core.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScoreboardManager {

    private final PlasmaCore plugin;
    private final Map<UUID, Scoreboard> playerBoards = new HashMap<>();

    public ScoreboardManager(PlasmaCore plugin) {
        this.plugin = plugin;
        startUpdateTask();
    }

    private void startUpdateTask() {
        int ticks = plugin.getConfig().getInt("scoreboard.update-ticks", 20);
        
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (plugin.getAuthManager().isLoggedIn(player)) {
                        updateScoreboard(player);
                    }
                }
            }
        }.runTaskTimer(plugin, 40L, ticks);
    }

    public void showScoreboard(Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        String title = MessageUtils.colorString(plugin.getConfig().getString("scoreboard.title", "&b&lPLASMA"));
        
        Objective obj = board.registerNewObjective("plasma", Criteria.DUMMY, MessageUtils.color(title));
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        playerBoards.put(player.getUniqueId(), board);
        player.setScoreboard(board);
        updateScoreboard(player);
    }

    public void updateScoreboard(Player player) {
        Scoreboard board = playerBoards.get(player.getUniqueId());
        if (board == null) return;

        Objective obj = board.getObjective("plasma");
        if (obj == null) return;

        // Очищаем старые
        for (String entry : board.getEntries()) {
            board.resetScores(entry);
        }

        // Данные
        double coins = plugin.getCoinsManager().getBalance(player);
        int online = Bukkit.getOnlinePlayers().size();
        int x = player.getLocation().getBlockX();
        int y = player.getLocation().getBlockY();
        int z = player.getLocation().getBlockZ();

        // Линии из конфига
        String[] defaultLines = {
            "",
            "&fИгрок: &a{player}",
            "&fБаланс: &e{coins}⛃",
            "",
            "&fОнлайн: &a{online}",
            "&fКоординаты:",
            "&7{x} {y} {z}",
            "",
            "&bplasma.mc.20tps.monster"
        };

        int score = defaultLines.length;
        for (String line : defaultLines) {
            line = line
                .replace("{player}", player.getName())
                .replace("{coins}", String.format("%.0f", coins))
                .replace("{online}", String.valueOf(online))
                .replace("{x}", String.valueOf(x))
                .replace("{y}", String.valueOf(y))
                .replace("{z}", String.valueOf(z));

            // Уникальные линии (добавляем невидимые символы)
            String uniqueLine = MessageUtils.colorString(line);
            while (board.getEntries().contains(uniqueLine)) {
                uniqueLine += "§r";
            }

            obj.getScore(uniqueLine).setScore(score--);
        }
    }

    public void hideScoreboard(Player player) {
        playerBoards.remove(player.getUniqueId());
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }
}
