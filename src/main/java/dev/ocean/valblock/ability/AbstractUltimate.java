package dev.ocean.valblock.game.ability;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public abstract class AbstractAbility {
    
    protected final String name;
    protected final String displayName;
    protected final AbilityType type;
    protected final Material iconMaterial;
    protected final int maxCharges;
    protected final int cost; // Credits cost to buy
    protected final double cooldown; // Cooldown in seconds
    
    protected String description;
    protected List<String> lore;
    protected boolean requiresTarget = false;
    protected double range = 0.0;
    protected double duration = 0.0;
    
    /**
     * Execute the ability
     * @param player The player using the ability
     * @return true if ability was successfully used
     */
    public abstract boolean execute(Player player);
    
    /**
     * Execute the ability with target location
     * @param player The player using the ability
     * @param targetX Target X coordinate
     * @param targetY Target Y coordinate  
     * @param targetZ Target Z coordinate
     * @return true if ability was successfully used
     */
    public boolean execute(Player player, double targetX, double targetY, double targetZ) {
        return execute(player); // Default implementation
    }
    
    /**
     * Check if player can use this ability
     */
    public abstract boolean canUse(Player player);
    
    /**
     * Called when ability effect ends
     */
    public void onEffectEnd(Player player) {
        // Override in subclasses if needed
    }
    
    /**
     * Get ability icon for inventory
     */
    public ItemStack getIcon() {
        ItemStack icon = new ItemStack(iconMaterial);
        var meta = icon.getItemMeta();
        meta.setDisplayName("§b" + displayName);
        
        var loreList = new java.util.ArrayList<String>();
        loreList.add("§7" + description);
        loreList.add("");
        loreList.add("§7Type: §f" + type.getDisplayName());
        if (cost > 0) loreList.add("§7Cost: §6" + cost + " credits");
        if (cooldown > 0) loreList.add("§7Cooldown: §f" + cooldown + "s");
        if (maxCharges > 1) loreList.add("§7Max Charges: §f" + maxCharges);
        if (range > 0) loreList.add("§7Range: §f" + range + " blocks");
        if (duration > 0) loreList.add("§7Duration: §f" + duration + "s");
        
        if (lore != null) {
            loreList.add("");
            loreList.addAll(lore);
        }
        
        meta.setLore(loreList);
        icon.setItemMeta(meta);
        return icon;
    }
    
    /**
     * Get remaining cooldown for player
     */
    public double getRemainingCooldown(UUID playerId) {
        // Implementation handled by AbilityManager
        return 0.0;
    }
    
    public enum AbilityType {
        BASIC("§7Basic", "Basic ability"),
        SIGNATURE("§aSignature", "Signature ability - free each round"),
        ULTIMATE("§6Ultimate", "Ultimate ability - requires ultimate points");
        
        private final String displayName;
        private final String description;
        
        AbilityType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
}