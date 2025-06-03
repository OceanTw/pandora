package dev.ocean.valblock.manager;

import dev.ocean.valblock.core.VMatch;
import dev.ocean.valblock.core.VPlayer;
import dev.ocean.valblock.game.map.AbstractMap;
import dev.ocean.valblock.game.map.AbstractMap.TeamSide;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class MatchManager {
    
    private final Map<UUID, VMatch> matches = new ConcurrentHashMap<>();
    private final Map<UUID, VMatch> playerMatches = new ConcurrentHashMap<>();
    private final Queue<VPlayer> matchmakingQueue = new LinkedList<>();
    
    /**
     * Create a new match
     */
    public VMatch createMatch(AbstractMap map, VMatch.MatchType type) {
        VMatch match = new VMatch(map, type);
        matches.put(match.getMatchId(), match);
        return match;
    }
    
    /**
     * Get match by ID
     */
    public VMatch getMatch(UUID matchId) {
        return matches.get(matchId);
    }
    
    /**
     * Get match by player
     */
    public VMatch getMatch(VPlayer player) {
        return playerMatches.get(player.getUuid());
    }
    
    /**
     * Get match by Bukkit player
     */
    public VMatch getMatch(Player bukkitPlayer) {
        return playerMatches.get(bukkitPlayer.getUniqueId());
    }
    
    /**
     * Join player to match
     */
    public boolean joinMatch(VPlayer player, UUID matchId, TeamSide preferredTeam) {
        VMatch match = matches.get(matchId);
        if (match == null) return false;
        
        // Remove from current match if in one
        if (player.isInMatch()) {
            leaveMatch(player);
        }
        
        if (match.addPlayer(player, preferredTeam)) {
            playerMatches.put(player.getUuid(), match);
            return true;
        }
        
        return false;
    }
    
    /**
     * Leave match
     */
    public void leaveMatch(VPlayer player) {
        VMatch match = playerMatches.remove(player.getUuid());
        if (match != null) {
            match.removePlayer(player);
            
            // Remove match if empty
            if (match.getAllPlayers().isEmpty()) {
                matches.remove(match.getMatchId());
            }
        }
    }
    
    /**
     * Add player to matchmaking queue
     */
    public void joinQueue(VPlayer player) {
        if (!matchmakingQueue.contains(player) && !player.isInMatch()) {
            matchmakingQueue.offer(player);
            tryMatchmaking();
        }
    }
    
    /**
     * Remove player from matchmaking queue
     */
    public void leaveQueue(VPlayer player) {
        matchmakingQueue.remove(player);
    }
    
    /**
     * Try to create matches from queue
     */
    private void tryMatchmaking() {
        if (matchmakingQueue.size() >= 4) { // Minimum players for a match
            List<VPlayer> players = new ArrayList<>();
            
            // Take up to 10 players from queue
            for (int i = 0; i < 10 && !matchmakingQueue.isEmpty(); i++) {
                players.add(matchmakingQueue.poll());
            }
            
            if (players.size() >= 4) {
                createMatchFromQueue(players);
            }
        }
    }
    
    /**
     * Create match from queued players
     */
    private void createMatchFromQueue(List<VPlayer> players) {
        // For now, use a default map (this should be improved with map selection)
        MapManager mapManager = dev.ocean.valblock.Plugin.getInstance().getMapManager();
        AbstractMap map = mapManager.getRandomMap();
        
        if (map == null) return;
        
        VMatch match = createMatch(map, VMatch.MatchType.UNRATED);
        
        // Distribute players between teams
        Collections.shuffle(players);
        for (int i = 0; i < players.size(); i++) {
            TeamSide team = (i % 2 == 0) ? TeamSide.ATTACKER : TeamSide.DEFENDER;
            joinMatch(players.get(i), match.getMatchId(), team);
        }
    }
    
    /**
     * Get all active matches
     */
    public Collection<VMatch> getActiveMatches() {
        return matches.values().stream()
                .filter(match -> match.getState() == VMatch.MatchState.ACTIVE)
                .toList();
    }
    
    /**
     * Get waiting matches (looking for players)
     */
    public Collection<VMatch> getWaitingMatches() {
        return matches.values().stream()
                .filter(match -> match.getState() == VMatch.MatchState.WAITING)
                .toList();
    }
    
    /**
     * End all matches (for plugin shutdown)
     */
    public void endAllMatches() {
        for (VMatch match : new ArrayList<>(matches.values())) {
            match.endMatch(VMatch.MatchEndReason.ADMIN_ENDED);
        }
        matches.clear();
        playerMatches.clear();
        matchmakingQueue.clear();
    }
    
    /**
     * Get match statistics
     */
    public MatchStats getStats() {
        MatchStats stats = new MatchStats();
        
        for (VMatch match : matches.values()) {
            stats.totalMatches++;
            
            switch (match.getState()) {
                case WAITING -> stats.waitingMatches++;
                case STARTING -> stats.startingMatches++;
                case ACTIVE -> stats.activeMatches++;
                case ENDED -> stats.endedMatches++;
            }
            
            stats.totalPlayers += match.getAllPlayers().size();
        }
        
        stats.queuedPlayers = matchmakingQueue.size();
        
        return stats;
    }
    
    /**
     * Force end a match
     */
    public boolean forceEndMatch(UUID matchId) {
        VMatch match = matches.get(matchId);
        if (match != null) {
            match.endMatch(VMatch.MatchEndReason.ADMIN_ENDED);
            return true;
        }
        return false;
    }
    
    @Getter
    public static class MatchStats {
        private int totalMatches = 0;
        private int waitingMatches = 0;
        private int startingMatches = 0;
        private int activeMatches = 0;
        private int endedMatches = 0;
        private int totalPlayers = 0;
        private int queuedPlayers = 0;
    }
}