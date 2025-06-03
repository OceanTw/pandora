package dev.ocean.valblock.weapon;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@Data
@RequiredArgsConstructor
public abstract class AbstractWeapon {
    
    protected final String name;
    protected final String displayName;
    protected final WeaponType type;
    protected final Material material;
    protected final int cost;
    
    // Damage values
    protected double headDamage;
    protected double bodyDamage;
    protected double legDamage;
    
    // Weapon stats
    protected double fireRate; // Rounds per second
    protected int magazineSize;
    protected int reserveAmmo;
    protected double reloadTime; // Seconds
    protected double range; // Effective range in blocks
    protected double accuracy; // 0.0 to 1.0
    protected boolean isAutomatic = false;
    protected boolean isSemiAuto = true;
    
    // Recoil and spread
    protected double recoilPattern; // Vertical recoil multiplier
    protected double horizontalSpread; // Horizontal spread
    protected double movementInaccuracy; // Accuracy penalty while moving
    protected double firstShotAccuracy; // Accuracy bonus for first shot
    
    // Special properties
    protected boolean canWallPenetrate = false;
    protected double wallPenetration = 0.0; // 0.0 to 1.0
    protected boolean hasScope = false;
    protected double scopeMultiplier = 1.0;
    
    protected String description;
    protected List<String> lore;
    
    /**
     * Called when weapon is fired
     * @param shooter The player shooting
     * @param target The target location or entity
     * @return true if shot was successful
     */
    public abstract boolean onFire(Player shooter);
    
    /**
     * Called when weapon hits a target
     * @param shooter The player who shot
     * @param target The player who was hit
     * @param hitLocation Where the shot hit (HEAD, BODY, LEG)
     * @return The damage dealt
     */
    public abstract double onHit(Player shooter, Player target, HitLocation hitLocation);
    
    /**
     * Called when weapon is reloaded
     */
    public abstract void onReload(Player player);
    
    /**
     * Check if player can use this weapon
     */
    public boolean canUse(Player player) {
        return player.hasPermission("valorant.weapon." + name.toLowerCase()) || 
               player.hasPermission("valorant.weapon.*");
    }
    
    /**
     * Get weapon damage based on hit location
     */
    public double getDamage(HitLocation hitLocation) {
        return switch (hitLocation) {
            case HEAD -> headDamage;
            case BODY -> bodyDamage;
            case LEG -> legDamage;
        };
    }
    
    /**
     * Calculate damage with range falloff
     */
    public double calculateDamageWithFalloff(HitLocation hitLocation, double distance) {
        double baseDamage = getDamage(hitLocation);
        
        if (distance <= range) {
            return baseDamage;
        }
        
        // Apply damage falloff for distances beyond effective range
        double falloffMultiplier = Math.max(0.1, 1.0 - ((distance - range) / range));
        return baseDamage * falloffMultiplier;
    }
    
    /**
     * Get weapon icon for shop/inventory
     */
    public ItemStack getIcon() {
        ItemStack icon = new ItemStack(material);
        var meta = icon.getItemMeta();
        meta.setDisplayName("§f" + displayName);
        
        var loreList = new java.util.ArrayList<String>();
        loreList.add("§7" + description);
        loreList.add("");
        loreList.add("§7Type: §f" + type.getDisplayName());
        loreList.add("§7Cost: §6" + cost + " credits");
        loreList.add("");
        loreList.add("§7Damage:");
        loreList.add("  §cHead: §f" + (int)headDamage);
        loreList.add("  §eBody: §f" + (int)bodyDamage);
        loreList.add("  §aLeg: §f" + (int)legDamage);
        loreList.add("");
        loreList.add("§7Fire Rate: §f" + fireRate + "/s");
        loreList.add("§7Magazine: §f" + magazineSize);
        if (hasScope) loreList.add("§7Scope: §a✓");
        if (canWallPenetrate) loreList.add("§7Wall Penetration: §a✓");
        
        if (lore != null) {
            loreList.add("");
            loreList.addAll(lore);
        }
        
        meta.setLore(loreList);
        icon.setItemMeta(meta);
        return icon;
    }
    
    public enum WeaponType {
        SIDEARM("§7Sidearm", "Secondary weapon"),
        SMG("§eSMG", "Submachine gun - high mobility"),
        RIFLE("§cRifle", "Assault rifle - balanced"),
        SNIPER("§5Sniper", "Sniper rifle - high damage, low mobility"),
        SHOTGUN("§6Shotgun", "Shotgun - devastating at close range"),
        HEAVY("§4Heavy", "Heavy weapon - high damage, low mobility"),
        MELEE("§fMelee", "Melee weapon");
        
        private final String displayName;
        private final String description;
        
        WeaponType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
    }
    
    public enum HitLocation {
        HEAD(2.0),
        BODY(1.0),
        LEG(0.9);
        
        private final double multiplier;
        
        HitLocation(double multiplier) {
            this.multiplier = multiplier;
        }
        
        public double getMultiplier() { return multiplier; }
    }
}