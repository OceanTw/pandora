package dev.ocean.valblock.core;

import dev.ocean.valblock.agent.AbstractAgent;
import dev.ocean.valblock.weapon.AbstractWeapon;
import dev.ocean.valblock.map.AbstractMap.TeamSide;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.entity.Player;

import java.util.*;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class VPlayer {
    
    @EqualsAndHashCode.Include
    private final UUID uuid;
    private final String name;
    
    // Current match data
    private VMatch currentMatch;
    private TeamSide teamSide;
    private AbstractAgent selectedAgent;
    
    // Combat stats
    private int health = 100;
    private int armor = 0;
    private int credits = 800; // Starting credits
    private int ultimatePoints = 0;
    
    // Loadout
    private AbstractWeapon primaryWeapon;
    private AbstractWeapon secondaryWeapon;
    private AbstractWeapon meleeWeapon;
    private Map<String, Integer> abilityCharges = new HashMap<>();
    
    // Round statistics
    private PlayerStats roundStats = new PlayerStats();
    private PlayerStats matchStats = new PlayerStats();
    private PlayerStats overallStats = new PlayerStats();
    
    // Game state
    private boolean isAlive = true;
    private boolean isDefusing = false;
    private boolean isPlanting = false;
    private long lastDamageTime = 0;
    private UUID lastDamager;
    
    // Cooldowns
    private Map<String, Long> abilityCooldowns = new HashMap<>();
    private long lastShotTime = 0;
    private boolean isReloading = false;
    
    public VPlayer(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }
    
    /**
     * Get the Bukkit player object
     */
    public Player getBukkitPlayer() {
        return org.bukkit.Bukkit.getPlayer(uuid);
    }
    
    /**
     * Check if player is in a match
     */
    public boolean isInMatch() {
        return currentMatch != null;
    }
    
    /**
     * Check if player is spectating
     */
    public boolean isSpectating() {
        return currentMatch != null && !isAlive;
    }
    
    /**
     * Add credits to player
     */
    public void addCredits(int amount) {
        this.credits = Math.max(0, this.credits + amount);
    }
    
    /**
     * Remove credits from player
     */
    public boolean removeCredits(int amount) {
        if (credits >= amount) {
            credits -= amount;
            return true;
        }
        return false;
    }
    
    /**
     * Add ultimate points
     */
    public void addUltimatePoints(int points) {
        this.ultimatePoints = Math.min(7, this.ultimatePoints + points); // Max 7 ult points
    }
    
    /**
     * Use ultimate points
     */
    public boolean useUltimatePoints(int points) {
        if (ultimatePoints >= points) {
            ultimatePoints -= points;
            return true;
        }
        return false;
    }
    
    /**
     * Deal damage to this player
     */
    public void takeDamage(double damage, VPlayer attacker) {
        // Apply armor reduction
        if (armor > 0) {
            double armorReduction = Math.min(damage * 0.5, armor);
            armor -= (int) armorReduction;
            damage -= armorReduction;
        }
        
        health -= (int) damage;
        lastDamageTime = System.currentTimeMillis();
        if (attacker != null) {
            lastDamager = attacker.getUuid();
        }
        
        if (health <= 0) {
            die(attacker);
        }
    }
    
    /**
     * Heal this player
     */
    public void heal(int amount) {
        this.health = Math.min(100, this.health + amount);
    }
    
    /**
     * Kill this player
     */
    public void die(VPlayer killer) {
        isAlive = false;
        health = 0;
        
        // Update stats
        roundStats.deaths++;
        matchStats.deaths++;
        overallStats.deaths++;
        
        if (killer != null && killer != this) {
            killer.getRoundStats().kills++;
            killer.getMatchStats().kills++;
            killer.getOverallStats().kills++;
            killer.addCredits(200); // Kill reward
            killer.addUltimatePoints(1); // Ult point for kill
        }
        
        // Clear abilities and weapons on death
        isDefusing = false;
        isPlanting = false;
    }
    
    /**
     * Respawn this player for new round
     */
    public void respawn() {
        isAlive = true;
        health = 100;
        isDefusing = false;
        isPlanting = false;
        lastDamageTime = 0;
        lastDamager = null;
        
        // Reset ability charges for new round
        if (selectedAgent != null) {
            // Signature ability gets one free charge per round
            String signatureAbilityName = selectedAgent.getSignatureAbility().getName();
            abilityCharges.put(signatureAbilityName, 1);
        }
    }
    
    /**
     * Start new round
     */
    public void startRound() {
        respawn();
        
        // Round economy
        if (currentMatch != null && currentMatch.getCurrentRound() > 1) {
            addCredits(getRoundEconomyBonus());
        }
    }
    
    /**
     * Get economy bonus for round based on performance
     */
    private int getRoundEconomyBonus() {
        int bonus = 1900; // Base round bonus
        
        // Loss bonus increases with consecutive losses
        if (currentMatch != null) {
            int lossStreak = currentMatch.getTeamLossStreak(teamSide);
            bonus += Math.min(lossStreak * 500, 2400); // Max 2400 loss bonus
        }
        
        return bonus;
    }
    
    /**
     * Check if ability is on cooldown
     */
    public boolean isAbilityOnCooldown(String abilityName) {
        Long cooldownEnd = abilityCooldowns.get(abilityName);
        return cooldownEnd != null && System.currentTimeMillis() < cooldownEnd;
    }
    
    /**
     * Set ability cooldown
     */
    public void setAbilityCooldown(String abilityName, double cooldownSeconds) {
        long cooldownEnd = System.currentTimeMillis() + (long)(cooldownSeconds * 1000);
        abilityCooldowns.put(abilityName, cooldownEnd);
    }
    
    /**
     * Get remaining cooldown for ability in seconds
     */
    public double getRemainingCooldown(String abilityName) {
        Long cooldownEnd = abilityCooldowns.get(abilityName);
        if (cooldownEnd == null) return 0.0;
        
        long remaining = cooldownEnd - System.currentTimeMillis();
        return Math.max(0.0, remaining / 1000.0);
    }
    
    /**
     * Reset all stats for new match
     */
    public void resetMatchStats() {
        matchStats = new PlayerStats();
        roundStats = new PlayerStats();
        credits = 800;
        ultimatePoints = 0;
        abilityCharges.clear();
        abilityCooldowns.clear();
    }
    
    /**
     * Reset round stats
     */
    public void resetRoundStats() {
        roundStats = new PlayerStats();
    }
    
    @Data
    public static class PlayerStats {
        private int kills = 0;
        private int deaths = 0;
        private int assists = 0;
        private int damage = 0;
        private int headshotKills = 0;
        private int abilityKills = 0;
        private int clutchWins = 0;
        private int plantsDefuses = 0;
        private int firstBloods = 0;
        private int multikills = 0;
        
        public double getKDRatio() {
            return deaths == 0 ? kills : (double) kills / deaths;
        }
        
        public double getADR() {
            return damage / Math.max(1, kills + deaths + assists);
        }
        
        public double getHeadshotPercentage() {
            return kills == 0 ? 0.0 : (double) headshotKills / kills * 100;
        }
    }
}