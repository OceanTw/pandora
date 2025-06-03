package dev.ocean.valblock.agent;

import dev.ocean.valblock.ability.AbstractAbility;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@Data
@RequiredArgsConstructor
public abstract class AbstractAgent {
    
    protected final String name;
    protected final String displayName;
    protected final AgentRole role;
    protected final Material skinMaterial;
    protected final String description;
    
    // Abilities
    protected AbstractAbility basicAbility;
    protected AbstractAbility signatureAbility;
    protected AbstractAbility ultimateAbility;
    
    // Stats
    protected int maxHealth = 100;
    protected int maxArmor = 50;
    protected double movementSpeed = 1.0;
    
    // Visual data
    protected String skinTexture; // Base64 texture for player head
    protected List<String> lore;
    
    public abstract void onAgentSelect(Player player);
    public abstract void onAgentDeselect(Player player);
    public abstract void onRoundStart(Player player);
    public abstract void onRoundEnd(Player player);
    public abstract void onDeath(Player player);
    public abstract void onKill(Player killer, Player victim);
    
    /**
     * Get the agent's icon for GUI displays
     */
    public ItemStack getIcon() {
        ItemStack icon = new ItemStack(skinMaterial);
        var meta = icon.getItemMeta();
        meta.setDisplayName("§a" + displayName);
        meta.setLore(lore);
        icon.setItemMeta(meta);
        return icon;
    }
    
    /**
     * Check if player can use this agent
     */
    public boolean canUse(Player player) {
        return player.hasPermission("valorant.agent." + name.toLowerCase()) || 
               player.hasPermission("valorant.agent.*");
    }
    
    /**
     * Apply agent-specific effects to player
     */
    public void applyEffects(Player player) {
        // Override in subclasses for agent-specific effects
    }
    
    /**
     * Remove agent-specific effects from player
     */
    public void removeEffects(Player player) {
        // Override in subclasses to clean up effects
    }
    
    public enum AgentRole {
        DUELIST("§cDuelist", "Self-sufficient fraggers"),
        INITIATOR("§eInitiator", "Prepare the team for battles"),
        CONTROLLER("§9Controller", "Cut up dangerous territory"),
        SENTINEL("§aSentinel", "Lock down flanks and watch team's back");
        
        private final String displayName;
        private final String description;
        
        AgentRole(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
}