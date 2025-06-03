package dev.ocean.valblock.core;

import dev.ocean.valblock.game.map.AbstractMap;
import dev.ocean.valblock.game.map.AbstractMap.TeamSide;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class VMatch {
    
    @EqualsAndHashCode.Include
    private final UUID matchId;
    private final AbstractMap map;
    private final MatchType type;
    private final int maxPlayersPerTeam;
    
    // Teams
    private final Set<VPlayer> attackers = ConcurrentHashMap.newKeySet();
    private final Set<VPlayer> defenders = ConcurrentHashMap.newKeySet();
    private final Set<VPlayer> spectators = ConcurrentHashMap.newKeySet();
    
    // Match state
    private MatchState state = MatchState.WAITING;
    private int currentRound = 0;
    private int attackerScore = 0;
    private int defenderScore = 0;
    private RoundState roundState = RoundState.WAITING;
    
    // Round timers
    private BukkitTask roundTimer;
    private BukkitTask buyTimer;
    private long roundStartTime;
    private long buyPhaseEndTime;
    
    // Spike mechanics
    private boolean spikeSpawned = false;
    private Location spikeLocation;
    private VPlayer spikeCarrier;
    private boolean spikePlanted = false;
    private Location plantedSpikeLocation;
    private long spikePlantTime;
    private BukkitTask spikeTimer;
    
    // Economy tracking
    private Map<TeamSide, Integer> teamLossStreak = new HashMap<>();
    
    // Match history
    private List<RoundResult> roundHistory = new ArrayList<>();
    
    public VMatch(AbstractMap map, MatchType type) {
        this.matchId = UUID.randomUUID();
        this.map = map;
        this.type = type;
        this.maxPlayersPerTeam = type.getMaxPlayersPerTeam();
        
        // Initialize loss streaks
        teamLossStreak.put(TeamSide.ATTACKER, 0);
        teamLossStreak.put(TeamSide.DEFENDER, 0);
    }
    
    /**
     * Add player to match
     */
    public boolean addPlayer(VPlayer player, TeamSide preferredTeam) {
        if (state != MatchState.WAITING) return false;
        
        Set<VPlayer> team = preferredTeam == TeamSide.ATTACKER ? attackers : defenders;
        Set<VPlayer> otherTeam = preferredTeam == TeamSide.ATTACKER ? defenders : attackers;
        
        // Auto-balance teams
        if (team.size() >= maxPlayersPerTeam) {
            if (otherTeam.size() < maxPlayersPerTeam) {
                team = otherTeam;
                preferredTeam = preferredTeam.getOpposite();
            } else {
                return false; // Match is full
            }
        }
        
        team.add(player);
        player.setCurrentMatch(this);
        player.setTeamSide(preferredTeam);
        player.resetMatchStats();
        
        // Check if match can start
        if (canStart()) {
            state = MatchState.STARTING;
            startMatch();
        }
        
        return true;
    }
    
    /**
     * Remove player from match
     */
    public void removePlayer(VPlayer player) {
        attackers.remove(player);
        defenders.remove(player);
        spectators.remove(player);
        
        player.setCurrentMatch(null);
        player.setTeamSide(null);
        
        // Handle match end if not enough players
        if (state == MatchState.ACTIVE && getAllPlayers().size() < type.getMinPlayers()) {
            endMatch(MatchEndReason.NOT_ENOUGH_PLAYERS);
        }
    }
    
    /**
     * Check if match can start
     */
    public boolean canStart() {
        return attackers.size() >= type.getMinPlayersPerTeam() && 
               defenders.size() >= type.getMinPlayersPerTeam();
    }
    
    /**
     * Start the match
     */
    public void startMatch() {
        state = MatchState.ACTIVE;
        currentRound = 1;
        
        // Spawn spike for attackers
        spawnSpike();
        
        // Start first round
        startRound();
        
        // Notify all players
        broadcast("§a§lMatch Started! §7Playing on " + map.getDisplayName());
        broadcast("§7First to §e" + type.getRoundsToWin() + " §7rounds wins!");
    }
    
    /**
     * Start a new round
     */
    public void startRound() {
        roundState = RoundState.BUY_PHASE;
        roundStartTime = System.currentTimeMillis();
        buyPhaseEndTime = roundStartTime + (map.getBuyTimeSeconds() * 1000L);
        
        // Reset all players for new round
        for (VPlayer player : getAllPlayers()) {
            player.startRound();
            spawnPlayer(player);
        }
        
        // Handle spike
        if (!spikePlanted) {
            spawnSpike();
        }
        
        // Start buy phase timer
        startBuyPhaseTimer();
        
        broadcast("§e§lRound " + currentRound + " §7- §aBuy Phase");
        broadcast("§7Buy time: §f" + map.getBuyTimeSeconds() + " seconds");
    }
    
    /**
     * Spawn player at appropriate location
     */
    private void spawnPlayer(VPlayer player) {
        Player bukkitPlayer = player.getBukkitPlayer();
        if (bukkitPlayer == null) return;
        
        Location spawnLocation = map.getRandomSpawn(player.getTeamSide());
        bukkitPlayer.teleport(spawnLocation);
        
        // Apply agent effects
        if (player.getSelectedAgent() != null) {
            player.getSelectedAgent().applyEffects(bukkitPlayer);
        }
    }
    
    /**
     * Start buy phase timer
     */
    private void startBuyPhaseTimer() {
        buyTimer = new BukkitRunnable() {
            @Override
            public void run() {
                endBuyPhase();
            }
        }.runTaskLater(dev.ocean.valblock.Plugin.getInstance(), map.getBuyTimeSeconds() * 20L);
    }
    
    /**
     * End buy phase and start combat phase
     */
    private void endBuyPhase() {
        roundState = RoundState.COMBAT;
        
        // Start round timer
        startRoundTimer();
        
        broadcast("§c§lCombat Phase Started!");
        broadcast("§7Time remaining: §f" + map.getRoundTimeSeconds() + " seconds");
        
        // Trigger round start events
        map.onRoundStart();
        for (VPlayer player : getAllPlayers()) {
            if (player.getSelectedAgent() != null) {
                player.getSelectedAgent().onRoundStart(player.getBukkitPlayer());
            }
        }
    }
    
    /**
     * Start round timer
     */
    private void startRoundTimer() {
        roundTimer = new BukkitRunnable() {
            @Override
            public void run() {
                endRound(RoundEndReason.TIME_EXPIRED, TeamSide.DEFENDER);
            }
        }.runTaskLater(dev.ocean.valblock.Plugin.getInstance(), map.getRoundTimeSeconds() * 20L);
    }
    
    /**
     * Plant the spike
     */
    public void plantSpike(VPlayer planter, Location location) {
        if (!canPlantSpike(planter, location)) return;
        
        spikePlanted = true;
        plantedSpikeLocation = location;
        spikePlantTime = System.currentTimeMillis();
        spikeCarrier = null;
        
        // Start spike timer (45 seconds to explode)
        spikeTimer = new BukkitRunnable() {
            @Override
            public void run() {
                explodeSpike();
            }
        }.runTaskLater(dev.ocean.valblock.Plugin.getInstance(), 45 * 20L);
        
        // Award planter and team
        planter.addCredits(300);
        planter.addUltimatePoints(1);
        planter.getRoundStats().setPlantsDefuses(planter.getRoundStats().getPlantsDefuses() + 1);
        
        broadcast("§c§lSpike Planted! §7" + planter.getName() + " planted the spike!");
        broadcast("§745 seconds until detonation!");
        
        // End round timer and start defuse timer
        if (roundTimer != null) {
            roundTimer.cancel();
        }
    }
    
    /**
     * Defuse the spike
     */
    public void defuseSpike(VPlayer defuser) {
        if (!canDefuseSpike(defuser)) return;
        
        spikePlanted = false;
        
        if (spikeTimer != null) {
            spikeTimer.cancel();
        }
        
        // Award defuser and team
        defuser.addCredits(300);
        defuser.addUltimatePoints(1);
        defuser.getRoundStats().setPlantsDefuses(defuser.getRoundStats().getPlantsDefuses() + 1);
        
        broadcast("§a§lSpike Defused! §7" + defuser.getName() + " defused the spike!");
        
        endRound(RoundEndReason.SPIKE_DEFUSED, TeamSide.DEFENDER);
    }
    
    /**
     * Explode the spike
     */
    private void explodeSpike() {
        broadcast("§c§lSpike Exploded! §7Attackers win the round!");
        endRound(RoundEndReason.SPIKE_EXPLODED, TeamSide.ATTACKER);
    }
    
    /**
     * Check if spike can be planted
     */
    private boolean canPlantSpike(VPlayer planter, Location location) {
        if (spikePlanted || planter != spikeCarrier) return false;
        if (planter.getTeamSide() != TeamSide.ATTACKER) return false;
        
        // Check if location is in a site
        return map.getSiteA().contains(location) || 
               map.getSiteB().contains(location) ||
               (map.isHasThreeSites() && map.getSiteC().contains(location));
    }
    
    /**
     * Check if spike can be defused
     */
    private boolean canDefuseSpike(VPlayer defuser) {
        if (!spikePlanted) return false;
        if (defuser.getTeamSide() != TeamSide.DEFENDER) return false;
        
        // Check if player is close enough to spike
        Player bukkitPlayer = defuser.getBukkitPlayer();
        return bukkitPlayer != null && 
               bukkitPlayer.getLocation().distance(plantedSpikeLocation) <= 2.0;
    }
    
    /**
     * Spawn spike for random attacker
     */
    private void spawnSpike() {
        if (attackers.isEmpty()) return;
        
        // Give spike to random attacker
        VPlayer randomAttacker = attackers.iterator().next();
        spikeCarrier = randomAttacker;
        spikeSpawned = true;
        
        // Give spike item to player
        Player bukkitPlayer = randomAttacker.getBukkitPlayer();
        if (bukkitPlayer != null) {
            // Add spike item to inventory
            org.bukkit.inventory.ItemStack spike = new org.bukkit.inventory.ItemStack(
                org.bukkit.Material.TNT);
            var meta = spike.getItemMeta();
            meta.setDisplayName("§c§lSpike");
            spike.setItemMeta(meta);
            bukkitPlayer.getInventory().addItem(spike);
        }
    }
    
    /**
     * End the current round
     */
    public void endRound(RoundEndReason reason, TeamSide winner) {
        roundState = RoundState.ENDED;
        
        // Cancel timers
        if (roundTimer != null) {
            roundTimer.cancel();
        }
        if (buyTimer != null) {
            buyTimer.cancel();
        }
        if (spikeTimer != null) {
            spikeTimer.cancel();
        }
        
        // Update scores
        if (winner == TeamSide.ATTACKER) {
            attackerScore++;
            teamLossStreak.put(TeamSide.ATTACKER, 0);
            teamLossStreak.put(TeamSide.DEFENDER, teamLossStreak.get(TeamSide.DEFENDER) + 1);
        } else {
            defenderScore++;
            teamLossStreak.put(TeamSide.DEFENDER, 0);
            teamLossStreak.put(TeamSide.ATTACKER, teamLossStreak.get(TeamSide.ATTACKER) + 1);
        }
        
        // Record round result
        RoundResult result = new RoundResult(currentRound, winner, reason, 
                                           new HashMap<>(getTeamStats(TeamSide.ATTACKER)),
                                           new HashMap<>(getTeamStats(TeamSide.DEFENDER)));
        roundHistory.add(result);
        
        // Broadcast round end
        broadcast("§e§lRound " + currentRound + " Complete!");
        broadcast("§7Winner: " + winner.getDisplayName());
        broadcast("§7Reason: §f" + reason.getDisplayName());
        broadcast("§7Score: §c" + attackerScore + "§7-§9" + defenderScore);
        
        // Check for match end
        if (attackerScore >= type.getRoundsToWin()) {
            endMatch(MatchEndReason.ATTACKERS_WON);
        } else if (defenderScore >= type.getRoundsToWin()) {
            endMatch(MatchEndReason.DEFENDERS_WON);
        } else if (currentRound >= type.getMaxRounds()) {
            endMatch(MatchEndReason.MAX_ROUNDS_REACHED);
        } else {
            // Check for side swap (at round 12 in competitive)
            if (currentRound == type.getRoundsToWin() - 1) {
                swapSides();
            }
            
            // Start next round after delay
            new BukkitRunnable() {
                @Override
                public void run() {
                    currentRound++;
                    startRound();
                }
            }.runTaskLater(dev.ocean.valblock.Plugin.getInstance(), 100L); // 5 second delay
        }
    }
    
    /**
     * Swap team sides
     */
    private void swapSides() {
        // Swap all players
        Set<VPlayer> tempAttackers = new HashSet<>(attackers);
        attackers.clear();
        attackers.addAll(defenders);
        defenders.clear();
        defenders.addAll(tempAttackers);
        
        // Update player team sides
        for (VPlayer player : attackers) {
            player.setTeamSide(TeamSide.ATTACKER);
        }
        for (VPlayer player : defenders) {
            player.setTeamSide(TeamSide.DEFENDER);
        }
        
        broadcast("§e§lSides Swapped! §7Attackers are now Defenders and vice versa!");
    }
    
    /**
     * End the match
     */
    public void endMatch(MatchEndReason reason) {
        state = MatchState.ENDED;
        
        // Cancel all timers
        if (roundTimer != null) roundTimer.cancel();
        if (buyTimer != null) buyTimer.cancel();
        if (spikeTimer != null) spikeTimer.cancel();
        
        // Determine winner
        TeamSide winner = null;
        if (attackerScore > defenderScore) {
            winner = TeamSide.ATTACKER;
        } else if (defenderScore > attackerScore) {
            winner = TeamSide.DEFENDER;
        }
        
        // Broadcast match end
        broadcast("§6§l=== MATCH COMPLETE ===");
        if (winner != null) {
            broadcast("§7Winner: " + winner.getDisplayName());
        } else {
            broadcast("§7Result: §eDraw");
        }
        broadcast("§7Final Score: §c" + attackerScore + "§7-§9" + defenderScore);
        broadcast("§7Reason: §f" + reason.getDisplayName());
        
        // Show match statistics
        showMatchStats();
        
        // Clean up players
        for (VPlayer player : getAllPlayers()) {
            player.setCurrentMatch(null);
            player.setTeamSide(null);
        }
    }
    
    /**
     * Show match statistics
     */
    private void showMatchStats() {
        broadcast("§6§l=== MATCH STATISTICS ===");
        
        // Show top performers
        List<VPlayer> allPlayers = new ArrayList<>(getAllPlayers());
        allPlayers.sort((a, b) -> Integer.compare(
            b.getMatchStats().getKills(), a.getMatchStats().getKills()));
        
        broadcast("§7Top Fraggers:");
        for (int i = 0; i < Math.min(3, allPlayers.size()); i++) {
            VPlayer player = allPlayers.get(i);
            var stats = player.getMatchStats();
            broadcast(String.format("§e%d. §f%s §7- §c%d§7K §8%d§7D §a%d§7A", 
                i + 1, player.getName(), stats.getKills(), stats.getDeaths(), stats.getAssists()));
        }
    }
    
    /**
     * Get all players in match
     */
    public Set<VPlayer> getAllPlayers() {
        Set<VPlayer> all = new HashSet<>();
        all.addAll(attackers);
        all.addAll(defenders);
        return all;
    }
    
    /**
     * Get team stats
     */
    private Map<String, Object> getTeamStats(TeamSide team) {
        Set<VPlayer> teamPlayers = team == TeamSide.ATTACKER ? attackers : defenders;
        Map<String, Object> stats = new HashMap<>();
        
        int totalKills = teamPlayers.stream().mapToInt(p -> p.getRoundStats().getKills()).sum();
        int totalDeaths = teamPlayers.stream().mapToInt(p -> p.getRoundStats().getDeaths()).sum();
        int totalDamage = teamPlayers.stream().mapToInt(p -> p.getRoundStats().getDamage()).sum();
        
        stats.put("kills", totalKills);
        stats.put("deaths", totalDeaths);
        stats.put("damage", totalDamage);
        stats.put("playersAlive", teamPlayers.stream().mapToInt(p -> p.isAlive() ? 1 : 0).sum());
        
        return stats;
    }
    
    /**
     * Get team loss streak
     */
    public int getTeamLossStreak(TeamSide team) {
        return teamLossStreak.getOrDefault(team, 0);
    }
    
    /**
     * Broadcast message to all players
     */
    public void broadcast(String message) {
        for (VPlayer player : getAllPlayers()) {
            Player bukkitPlayer = player.getBukkitPlayer();
            if (bukkitPlayer != null) {
                bukkitPlayer.sendMessage(message);
            }
        }
    }
    
    // Enums and inner classes
    
    public enum MatchState {
        WAITING, STARTING, ACTIVE, ENDED
    }
    
    public enum RoundState {
        WAITING, BUY_PHASE, COMBAT, ENDED
    }
    
    public enum RoundEndReason {
        ELIMINATION("Team Elimination"),
        SPIKE_EXPLODED("Spike Exploded"),
        SPIKE_DEFUSED("Spike Defused"),
        TIME_EXPIRED("Time Expired");
        
        private final String displayName;
        
        RoundEndReason(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() { return displayName; }
    }
    
    public enum MatchEndReason {
        ATTACKERS_WON("Attackers Won"),
        DEFENDERS_WON("Defenders Won"),
        MAX_ROUNDS_REACHED("Maximum Rounds Reached"),
        NOT_ENOUGH_PLAYERS("Not Enough Players"),
        ADMIN_ENDED("Ended by Administrator");
        
        private final String displayName;
        
        MatchEndReason(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() { return displayName; }
    }
    
    public enum MatchType {
        UNRATED(5, 13, 25, 2, "Unrated"),
        COMPETITIVE(5, 13, 25, 2, "Competitive"),
        DEATHMATCH(10, 40, 1, 1, "Deathmatch"),
        SPIKE_RUSH(5, 7, 13, 1, "Spike Rush");
        
        private final int maxPlayersPerTeam;
        private final int roundsToWin;
        private final int maxRounds;
        private final int minPlayersPerTeam;
        private final String displayName;
        
        MatchType(int maxPlayersPerTeam, int roundsToWin, int maxRounds, int minPlayersPerTeam, String displayName) {
            this.maxPlayersPerTeam = maxPlayersPerTeam;
            this.roundsToWin = roundsToWin;
            this.maxRounds = maxRounds;
            this.minPlayersPerTeam = minPlayersPerTeam;
            this.displayName = displayName;
        }
        
        public int getMaxPlayersPerTeam() { return maxPlayersPerTeam; }
        public int getRoundsToWin() { return roundsToWin; }
        public int getMaxRounds() { return maxRounds; }
        public int getMinPlayersPerTeam() { return minPlayersPerTeam; }
        public int getMinPlayers() { return minPlayersPerTeam * 2; }
        public String getDisplayName() { return displayName; }
    }
    
    @Data
    @lombok.AllArgsConstructor
    public static class RoundResult {
        private final int roundNumber;
        private final TeamSide winner;
        private final RoundEndReason reason;
        private final Map<String, Object> attackerStats;
        private final Map<String, Object> defenderStats;
    }
}