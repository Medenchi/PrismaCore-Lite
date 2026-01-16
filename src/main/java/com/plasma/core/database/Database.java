package com.plasma.core.database;

import com.plasma.core.PlasmaCore;

import java.io.File;
import java.sql.*;
import java.util.UUID;

public class Database {

    private final PlasmaCore plugin;
    private Connection connection;

    public Database(PlasmaCore plugin) {
        this.plugin = plugin;
    }

    public void init() {
        try {
            File file = new File(plugin.getDataFolder(), "plasma.db");
            connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
            createTables();
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка БД: " + e.getMessage());
        }
    }

    private void createTables() throws SQLException {
        Statement stmt = connection.createStatement();

        stmt.execute("""
            CREATE TABLE IF NOT EXISTS players (
                uuid TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                password TEXT,
                coins REAL DEFAULT 100,
                last_ip TEXT,
                last_login INTEGER
            )
        """);

        stmt.execute("""
            CREATE TABLE IF NOT EXISTS homes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                uuid TEXT NOT NULL,
                name TEXT NOT NULL,
                world TEXT NOT NULL,
                x REAL NOT NULL,
                y REAL NOT NULL,
                z REAL NOT NULL,
                yaw REAL NOT NULL,
                pitch REAL NOT NULL,
                UNIQUE(uuid, name)
            )
        """);

        stmt.execute("""
            CREATE TABLE IF NOT EXISTS spawn (
                id INTEGER PRIMARY KEY DEFAULT 1,
                world TEXT NOT NULL,
                x REAL NOT NULL,
                y REAL NOT NULL,
                z REAL NOT NULL,
                yaw REAL NOT NULL,
                pitch REAL NOT NULL
            )
        """);

        stmt.execute("""
            CREATE TABLE IF NOT EXISTS sessions (
                uuid TEXT PRIMARY KEY,
                ip TEXT NOT NULL,
                expires INTEGER NOT NULL
            )
        """);

        stmt.close();
    }

    public Connection getConnection() { return connection; }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка закрытия БД: " + e.getMessage());
        }
    }

    // === PLAYERS ===

    public boolean playerExists(UUID uuid) {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT uuid FROM players WHERE uuid = ?");
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            boolean exists = rs.next();
            rs.close(); ps.close();
            return exists;
        } catch (SQLException e) { return false; }
    }

    public void createPlayer(UUID uuid, String name) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                "INSERT OR IGNORE INTO players (uuid, name, coins) VALUES (?, ?, ?)"
            );
            ps.setString(1, uuid.toString());
            ps.setString(2, name);
            ps.setDouble(3, plugin.getConfig().getDouble("coins.start-balance", 100));
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка создания игрока: " + e.getMessage());
        }
    }

    public boolean isRegistered(UUID uuid) {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT password FROM players WHERE uuid = ? AND password IS NOT NULL");
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            boolean registered = rs.next();
            rs.close(); ps.close();
            return registered;
        } catch (SQLException e) { return false; }
    }

    public void setPassword(UUID uuid, String password) {
        try {
            PreparedStatement ps = connection.prepareStatement("UPDATE players SET password = ? WHERE uuid = ?");
            ps.setString(1, password);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка установки пароля: " + e.getMessage());
        }
    }

    public String getPassword(UUID uuid) {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT password FROM players WHERE uuid = ?");
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            String pass = rs.next() ? rs.getString("password") : null;
            rs.close(); ps.close();
            return pass;
        } catch (SQLException e) { return null; }
    }

    // === SESSIONS ===

    public void createSession(UUID uuid, String ip, long expires) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                "INSERT OR REPLACE INTO sessions (uuid, ip, expires) VALUES (?, ?, ?)"
            );
            ps.setString(1, uuid.toString());
            ps.setString(2, ip);
            ps.setLong(3, expires);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка сессии: " + e.getMessage());
        }
    }

    public boolean hasValidSession(UUID uuid, String ip) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                "SELECT expires FROM sessions WHERE uuid = ? AND ip = ?"
            );
            ps.setString(1, uuid.toString());
            ps.setString(2, ip);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                boolean valid = System.currentTimeMillis() < rs.getLong("expires");
                rs.close(); ps.close();
                return valid;
            }
            rs.close(); ps.close();
            return false;
        } catch (SQLException e) { return false; }
    }

    // === COINS ===

    public double getCoins(UUID uuid) {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT coins FROM players WHERE uuid = ?");
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            double coins = rs.next() ? rs.getDouble("coins") : 0;
            rs.close(); ps.close();
            return coins;
        } catch (SQLException e) { return 0; }
    }

    public void setCoins(UUID uuid, double amount) {
        try {
            PreparedStatement ps = connection.prepareStatement("UPDATE players SET coins = ? WHERE uuid = ?");
            ps.setDouble(1, amount);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка установки монет: " + e.getMessage());
        }
    }

    public void addCoins(UUID uuid, double amount) {
        setCoins(uuid, getCoins(uuid) + amount);
    }

    public void removeCoins(UUID uuid, double amount) {
        setCoins(uuid, Math.max(0, getCoins(uuid) - amount));
    }

    // === HOMES ===

    public void setHome(UUID uuid, String name, String world, double x, double y, double z, float yaw, float pitch) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                "INSERT OR REPLACE INTO homes (uuid, name, world, x, y, z, yaw, pitch) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
            );
            ps.setString(1, uuid.toString());
            ps.setString(2, name.toLowerCase());
            ps.setString(3, world);
            ps.setDouble(4, x);
            ps.setDouble(5, y);
            ps.setDouble(6, z);
            ps.setFloat(7, yaw);
            ps.setFloat(8, pitch);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка сохранения дома: " + e.getMessage());
        }
    }

    public ResultSet getHome(UUID uuid, String name) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM homes WHERE uuid = ? AND name = ?"
            );
            ps.setString(1, uuid.toString());
            ps.setString(2, name.toLowerCase());
            return ps.executeQuery();
        } catch (SQLException e) { return null; }
    }

    public ResultSet getHomes(UUID uuid) {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT name FROM homes WHERE uuid = ?");
            ps.setString(1, uuid.toString());
            return ps.executeQuery();
        } catch (SQLException e) { return null; }
    }

    public int getHomesCount(UUID uuid) {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) as cnt FROM homes WHERE uuid = ?");
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            int count = rs.next() ? rs.getInt("cnt") : 0;
            rs.close(); ps.close();
            return count;
        } catch (SQLException e) { return 0; }
    }

    public void deleteHome(UUID uuid, String name) {
        try {
            PreparedStatement ps = connection.prepareStatement("DELETE FROM homes WHERE uuid = ? AND name = ?");
            ps.setString(1, uuid.toString());
            ps.setString(2, name.toLowerCase());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка удаления дома: " + e.getMessage());
        }
    }

    // === SPAWN ===

    public void setSpawn(String world, double x, double y, double z, float yaw, float pitch) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                "INSERT OR REPLACE INTO spawn (id, world, x, y, z, yaw, pitch) VALUES (1, ?, ?, ?, ?, ?, ?)"
            );
            ps.setString(1, world);
            ps.setDouble(2, x);
            ps.setDouble(3, y);
            ps.setDouble(4, z);
            ps.setFloat(5, yaw);
            ps.setFloat(6, pitch);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            plugin.getLogger().severe("Ошибка спавна: " + e.getMessage());
        }
    }

    public ResultSet getSpawn() {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM spawn WHERE id = 1");
            return ps.executeQuery();
        } catch (SQLException e) { return null; }
    }
}
