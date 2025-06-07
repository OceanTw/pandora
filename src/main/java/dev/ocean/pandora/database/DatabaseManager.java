package dev.ocean.pandora.database;

import dev.ocean.pandora.Pandora;
import dev.ocean.pandora.manager.ConfigManager;
import lombok.Getter;

import java.sql.*;
import java.util.UUID;

@Getter
public class DatabaseManager {
    private final Pandora plugin;
    private final ConfigManager configManager;
    private Connection connection;

    public DatabaseManager(Pandora plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        connect();
        createTables();
    }

    private void connect() {
        try {
            String type = configManager.getDatabaseType();
            String url = configManager.getDatabaseUrl();

            if (type.equalsIgnoreCase("sqlite")) {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection(url);
            } else if (type.equalsIgnoreCase("mysql")) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(
                        url,
                        configManager.getDatabaseUsername(),
                        configManager.getDatabasePassword()
                );
            }

            plugin.getLogger().info("Database connected successfully!");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to connect to database: " + e.getMessage());
        }
    }

    private void createTables() {
        try (Statement stmt = connection.createStatement()) {
            String playerStatsTable = """
                CREATE TABLE IF NOT EXISTS player_stats (
                    uuid VARCHAR(36) PRIMARY KEY,
                    username VARCHAR(16) NOT NULL,
                    wins INTEGER DEFAULT 0,
                    losses INTEGER DEFAULT 0,
                    kills INTEGER DEFAULT 0,
                    deaths INTEGER DEFAULT 0,
                    elo INTEGER DEFAULT 1000,
                    streak INTEGER DEFAULT 0,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """;

            String matchHistoryTable = """
                CREATE TABLE IF NOT EXISTS match_history (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    match_uuid VARCHAR(36) NOT NULL,
                    player1_uuid VARCHAR(36) NOT NULL,
                    player2_uuid VARCHAR(36) NOT NULL,
                    winner_uuid VARCHAR(36),
                    kit_name VARCHAR(32) NOT NULL,
                    arena_name VARCHAR(32) NOT NULL,
                    duration INTEGER NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """;

            stmt.execute(playerStatsTable);
            stmt.execute(matchHistoryTable);

        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create tables: " + e.getMessage());
        }
    }

    public PlayerStats getPlayerStats(UUID uuid) {
        String query = "SELECT * FROM player_stats WHERE uuid = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new PlayerStats(
                        UUID.fromString(rs.getString("uuid")),
                        rs.getString("username"),
                        rs.getInt("wins"),
                        rs.getInt("losses"),
                        rs.getInt("kills"),
                        rs.getInt("deaths"),
                        rs.getInt("elo"),
                        rs.getInt("streak")
                );
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get player stats: " + e.getMessage());
        }
        return null;
    }

    public void savePlayerStats(PlayerStats stats) {
        String query = """
            INSERT OR REPLACE INTO player_stats 
            (uuid, username, wins, losses, kills, deaths, elo, streak, updated_at) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
        """;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, stats.getUuid().toString());
            stmt.setString(2, stats.getUsername());
            stmt.setInt(3, stats.getWins());
            stmt.setInt(4, stats.getLosses());
            stmt.setInt(5, stats.getKills());
            stmt.setInt(6, stats.getDeaths());
            stmt.setInt(7, stats.getElo());
            stmt.setInt(8, stats.getStreak());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save player stats: " + e.getMessage());
        }
    }

    public void recordMatch(UUID matchUuid, UUID player1, UUID player2, UUID winner, String kitName, String arenaName, int duration) {
        String query = """
            INSERT INTO match_history 
            (match_uuid, player1_uuid, player2_uuid, winner_uuid, kit_name, arena_name, duration) 
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, matchUuid.toString());
            stmt.setString(2, player1.toString());
            stmt.setString(3, player2.toString());
            stmt.setString(4, winner != null ? winner.toString() : null);
            stmt.setString(5, kitName);
            stmt.setString(6, arenaName);
            stmt.setInt(7, duration);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to record match: " + e.getMessage());
        }
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to disconnect from database: " + e.getMessage());
        }
    }
}