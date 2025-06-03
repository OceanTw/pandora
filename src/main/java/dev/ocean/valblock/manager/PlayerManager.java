package dev.ocean.valblock.manager;

import dev.ocean.valblock.core.VPlayer;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class PlayerManager {
    
    private final Map<UUID, VPlayer> players = new ConcurrentHashMap<>();
    private final Map<String, VPlayer> playersByName = new ConcurrentHashMap<>();
    
    /**
     * Get or create VPlayer for Bukkit player
     */
    public VPlayer getPlayer(Player bukkitPlayer) {
        return getPlayer(bukkitPlayer.getUniqueId(), bukkitPlayer.getName());
    }
    
    /**
     * Get or create VPlayer by UUID and name
     */
    public VPlayer getPlayer(UUID uuid, String name) {
        VPlayer vPlayer = players.get(uuid);
        if (vPlayer == null) {
            vPlayer = new VPlayer(uuid, name);
            players.put(uuid, vPlayer);
            playersByName.put(name.toLowerCase(), vPlayer);
        }
        return vPlayer;
    }
    
    /**
     * Get VPlayer by UUID
     */
    public VPlayer getPlayer(UUID uuid) {
        return players.get(uuid);
    }
    
    /**
     * Get VPlayer by name
     */
    public VPlayer getPlayer(String name) {
        return playersByName.get(name.toLowerCase());
    }
    
    /**
     * Check if player exists
     */
    public boolean hasPlayer(UUID uuid) {
        return players.containsKey(uuid);
    }
    
    /**
     * Remove player from manager
     */
    public void removePlayer(UUID uuid) {
        VPlayer player = players.remove(uuid);
        if (player != null) {
            playersByName.remove(player.getName().toLowerCase());
            
            // Remove from match if in one
            if (player.isInMatch()) {
                player.getCurrentMatch().removePlayer(player);
            }
        }
    }
    
    /**
     * Get all players
     */
    public Collection<VPlayer> getAllPlayers() {
        return new ArrayList<>(players.values());
    }
    
    /**
     * Get all players in matches
     */
    public List<VPlayer> getPlayersInMatches() {
        return players.values().stream()
                .filter(VPlayer::isInMatch)
                .toList();
    }
    
    /**
     * Get all players not in matches
     */
    public List<VPlayer> getPlayersNotInMatches() {
        return players.values().stream()
                .filter(player -> !player.isInMatch())
                .toList();
    }
    
    /**
     * Update player name (in case of name change)
     */
    public void updatePlayerName(UUID uuid, String newName) {
        VPlayer player = players.get(uuid);
        if (player != null) {
            // Remove old name mapping
            playersByName.remove(player.getName().toLowerCase());
            
            // Update name and add new mapping
            playersByName.put(newName.toLowerCase(), player);
        }
    }
    
    /**
     * Get player statistics
     */
    public PlayerStats getGlobalStats() {
        PlayerStats stats = new PlayerStats();
        
        for (VPlayer player : players.values()) {
            stats.totalPlayers++;
            if (player.isInMatch()) {
                stats.playersInMatch++;
            } else {
                stats.playersInLobby++;
            }
            
            var playerStats = player.getOverallStats();
            stats.totalKills += playerStats.getKills();
            stats.totalDeaths += playerStats.getDeaths();
            stats.totalDamage += playerStats.getDamage();
        }
        
        return stats;
    }
    
    /**
     * Save player data
     */
    public void savePlayerData(VPlayer player) {
        // TODO: Implement database saving
    }
    
    /**
     * Load player data
     */
    public void loadPlayerData(VPlayer player) {
        // TODO: Implement database loading
    }
    
    @Getter
    public static class PlayerStats {
        private int totalPlayers = 0;
        private int playersInMatch = 0;
        private int playersInLobby = 0;
        private long totalKills = 0;
        private long totalDeaths = 0;
        private long totalDamage = 0;
        
        public double getAverageKDR() {
            return totalDeaths == 0 ? totalKills : (double) totalKills / totalDeaths;
        }
    }
}