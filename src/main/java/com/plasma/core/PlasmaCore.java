package com.plasma.core;

import com.plasma.core.auth.AuthManager;
import com.plasma.core.coins.CoinsManager;
import com.plasma.core.commands.*;
import com.plasma.core.database.Database;
import com.plasma.core.homes.HomesManager;
import com.plasma.core.scoreboard.ScoreboardManager;
import com.plasma.core.tab.TabManager;
import com.plasma.core.hud.HUDManager;
import com.plasma.core.tpa.TPAManager;
import com.plasma.core.utils.MessageUtils;
import org.bukkit.plugin.java.JavaPlugin;

public class PlasmaCore extends JavaPlugin {

    private static PlasmaCore instance;
    private Database database;
    private AuthManager authManager;
    private HomesManager homesManager;
    private CoinsManager coinsManager;
    private TPAManager tpaManager;
    private ScoreboardManager scoreboardManager;
    private TabManager tabManager;
    private HUDManager hudManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // Красивый старт
        getLogger().info("");
        getLogger().info("§b██████╗ ██╗      █████╗ ███████╗███╗   ███╗ █████╗");
        getLogger().info("§b██╔══██╗██║     ██╔══██╗██╔════╝████╗ ████║██╔══██╗");
        getLogger().info("§b██████╔╝██║     ███████║███████╗██╔████╔██║███████║");
        getLogger().info("§b██╔═══╝ ██║     ██╔══██║╚════██║██║╚██╔╝██║██╔══██║");
        getLogger().info("§b██║     ███████╗██║  ██║███████║██║ ╚═╝ ██║██║  ██║");
        getLogger().info("§b╚═╝     ╚══════╝╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝╚═╝  ╚═╝");
        getLogger().info("");

        // База данных
        database = new Database(this);
        database.init();
        getLogger().info("§a✓ База данных загружена");

        // Менеджеры
        authManager = new AuthManager(this);
        getLogger().info("§a✓ Авторизация загружена");

        homesManager = new HomesManager(this);
        getLogger().info("§a✓ Дома загружены");

        coinsManager = new CoinsManager(this);
        getLogger().info("§a✓ Экономика загружена");

        tpaManager = new TPAManager(this);
        getLogger().info("§a✓ TPA загружен");

        if (getConfig().getBoolean("scoreboard.enabled")) {
            scoreboardManager = new ScoreboardManager(this);
            getLogger().info("§a✓ Scoreboard загружен");
        }

        if (getConfig().getBoolean("tab.enabled")) {
            tabManager = new TabManager(this);
            getLogger().info("§a✓ Tab загружен");
        }

        if (getConfig().getBoolean("hud.enabled")) {
            hudManager = new HUDManager(this);
            getLogger().info("§a✓ HUD загружен");
        }

        // Команды
        registerCommands();
        getLogger().info("§a✓ Команды зарегистрированы");

        getLogger().info("");
        getLogger().info("§a§lPlasmaCore успешно запущен!");
        getLogger().info("");
    }

    @Override
    public void onDisable() {
        if (database != null) database.close();
        getLogger().info("§c✗ PlasmaCore отключён");
    }

    private void registerCommands() {
        getCommand("register").setExecutor(new RegisterCommand(this));
        getCommand("login").setExecutor(new LoginCommand(this));
        getCommand("home").setExecutor(new HomeCommand(this));
        getCommand("sethome").setExecutor(new SetHomeCommand(this));
        getCommand("delhome").setExecutor(new DelHomeCommand(this));
        getCommand("homes").setExecutor(new HomesCommand(this));
        getCommand("spawn").setExecutor(new SpawnCommand(this));
        getCommand("setspawn").setExecutor(new SetSpawnCommand(this));
        getCommand("tpa").setExecutor(new TPACommand(this));
        getCommand("tpaccept").setExecutor(new TPAcceptCommand(this));
        getCommand("tpdeny").setExecutor(new TPDenyCommand(this));
        getCommand("balance").setExecutor(new BalanceCommand(this));
        getCommand("pay").setExecutor(new PayCommand(this));
        getCommand("plasmareload").setExecutor(new ReloadCommand(this));
    }

    public static PlasmaCore getInstance() { return instance; }
    public Database getDatabase() { return database; }
    public AuthManager getAuthManager() { return authManager; }
    public HomesManager getHomesManager() { return homesManager; }
    public CoinsManager getCoinsManager() { return coinsManager; }
    public TPAManager getTpaManager() { return tpaManager; }
    public ScoreboardManager getScoreboardManager() { return scoreboardManager; }
    public TabManager getTabManager() { return tabManager; }
    public HUDManager getHudManager() { return hudManager; }
}
