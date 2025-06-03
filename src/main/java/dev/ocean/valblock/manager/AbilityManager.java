package dev.ocean.valblock.manager;

import dev.ocean.valblock.game.ability.AbstractAbility;
import dev.ocean.valblock.game.ability.impl.*;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class AbilityManager {
    
    private final Map<String, AbstractAbility> abilities = new ConcurrentHashMap<>();
    private final Map<AbstractAbility.AbilityType, List<AbstractAbility>> abilitiesByType = new EnumMap<>(AbstractAbility.AbilityType.class);
    
    // Cooldown tracking
    private final Map<UUID, Map<String, Long>> playerCooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Map<String, Integer>> playerCharges = new ConcurrentHashMap<>();
    
    // Active effects
    private final Map<UUID, Set<BukkitTask>> activeEffects = new ConcurrentHashMap<>();
    
    public AbilityManager() {
        // Initialize type lists
        for (AbstractAbility.AbilityType type : AbstractAbility.AbilityType.values()) {
            abilitiesByType.put(type, new ArrayList<>());
        }
    }
    
    /**
     * Register an ability
     */
    public void registerAbility(AbstractAbility ability) {
        abilities.put(ability.getName().toLowerCase(), ability);
        abilitiesByType.get(ability.getType()).add(ability);
    }
    
    /**
     * Get ability by name
     */
    public AbstractAbility getAbility(String name) {
        return abilities.get(name.toLowerCase());
    }
    
    /**
     * Get all abilities
     */
    public Collection<AbstractAbility> getAllAbilities() {
        return new ArrayList<>(abilities.values());
    }
    
    /**
     * Get abilities by type
     */
    public List<AbstractAbility> getAbilitiesByType(AbstractAbility.AbilityType type) {
        return new ArrayList<>(abilitiesByType.get(type));
    }
    
    /**
     * Use an ability
     */
    public boolean useAbility(Player player, String abilityName) {
        AbstractAbility ability = getAbility(abilityName);
        if (ability == null) return false;
        
        UUID playerId = player.getUniqueId();
        
        // Check if ability can be used
        if (!ability.canUse(player)) return false;
        
        // Check cooldown
        if (isAbilityOnCooldown(playerId, abilityName)) return false;
        
        // Check charges
        if (!hasCharges(playerId, abilityName)) return false;
        
        // Execute ability
        if (ability.execute(player)) {
            // Consume charge
            consumeCharge(playerId, abilityName);
            
            // Set cooldown
            setCooldown(playerId, abilityName, ability.getCooldown());
            
            // Handle duration effects
            if (ability.getDuration() > 0) {
                scheduleEffectEnd(player, ability);
            }
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Use ability with target location
     */
    public boolean useAbility(Player player, String abilityName, double x, double y, double z) {
        AbstractAbility ability = getAbility(abilityName);
        if (ability == null) return false;
        
        UUID playerId = player.getUniqueId();
        
        // Check if ability can be used
        if (!ability.canUse(player)) return false;
        
        // Check cooldown
        if (isAbilityOnCooldown(playerId, abilityName)) return false;
        
        // Check charges
        if (!hasCharges(playerId, abilityName)) return false;
        
        // Execute ability with target
        if (ability.execute(player, x, y, z)) {
            // Consume charge
            consumeCharge(playerId, abilityName);
            
            // Set cooldown
            setCooldown(playerId, abilityName, ability.getCooldown());
            
            // Handle duration effects
            if (ability.getDuration() > 0) {
                scheduleEffectEnd(player, ability);
            }
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Check if ability is on cooldown
     */
    public boolean isAbilityOnCooldown(UUID playerId, String abilityName) {
        Map<String, Long> cooldowns = playerCooldowns.get(playerId);
        if (cooldowns == null) return false;
        
        Long cooldownEnd = cooldowns.get(abilityName.toLowerCase());
        return cooldownEnd != null && System.currentTimeMillis() < cooldownEnd;
    }
    
    /**
     * Get remaining cooldown
     */
    public double getRemainingCooldown(UUID playerId, String abilityName) {
        Map<String, Long> cooldowns = playerCooldowns.get(playerId);
        if (cooldowns == null) return 0.0;
        
        Long cooldownEnd = cooldowns.get(abilityName.toLowerCase());
        if (cooldownEnd == null) return 0.0;
        
        long remaining = cooldownEnd - System.currentTimeMillis();
        return Math.max(0.0, remaining / 1000.0);
    }
    
    /**
     * Set ability cooldown
     */
    public void setCooldown(UUID playerId, String abilityName, double cooldownSeconds) {
        if (cooldownSeconds <= 0) return;
        
        Map<String, Long> cooldowns = playerCooldowns.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>());
        long cooldownEnd = System.currentTimeMillis() + (long)(cooldownSeconds * 1000);
        cooldowns.put(abilityName.toLowerCase(), cooldownEnd);
    }
    
    /**
     * Clear ability cooldown
     */
    public void clearCooldown(UUID playerId, String abilityName) {
        Map<String, Long> cooldowns = playerCooldowns.get(playerId);
        if (cooldowns != null) {
            cooldowns.remove(abilityName.toLowerCase());
        }
    }
    
    /**
     * Check if player has charges for ability
     */
    public boolean hasCharges(UUID playerId, String abilityName) {
        return getCharges(playerId, abilityName) > 0;
    }
    
    /**
     * Get player's charges for ability
     */
    public int getCharges(UUID playerId, String abilityName) {
        Map<String, Integer> charges = playerCharges.get(playerId);
        if (charges == null) return 0;
        
        return charges.getOrDefault(abilityName.toLowerCase(), 0);
    }
    
    /**
     * Set player's charges for ability
     */
    public void setCharges(UUID playerId, String abilityName, int charges) {
        Map<String, Integer> playerChargeMap = playerCharges.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>());
        
        AbstractAbility ability = getAbility(abilityName);
        if (ability != null) {
            charges = Math.max(0, Math.min(charges, ability.getMaxCharges()));
        }
        
        playerChargeMap.put(abilityName.toLowerCase(), charges);
    }
    
    /**
     * Add charges to ability
     */
    public void addCharges(UUID playerId, String abilityName, int charges) {
        int currentCharges = getCharges(playerId, abilityName);
        setCharges(playerId, abilityName, currentCharges + charges);
    }
    
    /**
     * Consume one charge
     */
    public void consumeCharge(UUID playerId, String abilityName) {
        int currentCharges = getCharges(playerId, abilityName);
        if (currentCharges > 0) {
            setCharges(playerId, abilityName, currentCharges - 1);
        }
    }
    
    /**
     * Reset player's ability data
     */
    public void resetPlayer(UUID playerId) {
        playerCooldowns.remove(playerId);
        playerCharges.remove(playerId);
        
        // Cancel active effects
        Set<BukkitTask> effects = activeEffects.remove(playerId);
        if (effects != null) {
            effects.forEach(BukkitTask::cancel);
        }
    }
    
    /**
     * Schedule ability effect end
     */
    private void scheduleEffectEnd(Player player, AbstractAbility ability) {
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                ability.onEffectEnd(player);
                
                // Remove from active effects
                Set<BukkitTask> effects = activeEffects.get(player.getUniqueId());
                if (effects != null) {
                    effects.remove(this);
                }
            }
        }.runTaskLater(dev.ocean.valblock.Plugin.getInstance(), (long)(ability.getDuration() * 20));
        
        // Track active effect
        Set<BukkitTask> effects = activeEffects.computeIfAbsent(player.getUniqueId(), k -> ConcurrentHashMap.newKeySet());
        effects.add(task);
    }
    
    /**
     * Load default abilities
     */
    public void loadDefaultAbilities() {
        // Basic abilities
        registerAbility(new FlashAbility());
        registerAbility(new SmokeAbility());
        registerAbility(new DashAbility());
        registerAbility(new HealAbility());
        registerAbility(new RevealAbility());
        
        // Signature abilities  
        registerAbility(new UpdraftAbility());
        registerAbility(new TailwindAbility());
        registerAbility(new HealingOrbAbility());
        registerAbility(new ReconBoltAbility());
        
        // Ultimate abilities
        registerAbility(new BladeStormAbility());
        registerAbility(new HuntersEdgeAbility());
        registerAbility(new ResurrectionAbility());
        registerAbility(new VipersPitAbility());
        
        dev.ocean.valblock.Plugin.getInstance().getLogger()
                .info("Loaded " + abilities.size() + " default abilities");
    }
    
    /**
     * Clean up expired cooldowns
     */
    public void cleanupExpiredCooldowns() {
        long currentTime = System.currentTimeMillis();
        
        for (Map<String, Long> cooldowns : playerCooldowns.values()) {
            cooldowns.entrySet().removeIf(entry -> entry.getValue() < currentTime);
        }
        
        // Remove empty maps
        playerCooldowns.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }
    
    /**
     * Get player ability statistics
     */
    public PlayerAbilityStats getPlayerStats(UUID playerId) {
        PlayerAbilityStats stats = new PlayerAbilityStats();
        
        Map<String, Long> cooldowns = playerCooldowns.get(playerId);
        Map<String, Integer> charges = playerCharges.get(playerId);
        
        if (cooldowns != null) {
            stats.abilitiesOnCooldown = (int) cooldowns.entrySet().stream()
                    .filter(entry -> entry.getValue() > System.currentTimeMillis())
                    .count();
        }
        
        if (charges != null) {
            stats.totalCharges = charges.values().stream().mapToInt(Integer::intValue).sum();
        }
        
        Set<BukkitTask> effects = activeEffects.get(playerId);
        if (effects != null) {
            stats.activeEffects = effects.size();
        }
        
        return stats;
    }
    
    @Getter
    public static class PlayerAbilityStats {
        private int abilitiesOnCooldown = 0;
        private int totalCharges = 0;
        private int activeEffects = 0;
    }
}