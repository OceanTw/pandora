package dev.ocean.valblock.manager;

import dev.ocean.valblock.weapon.AbstractWeapon;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class WeaponManager {
    
    private final Map<String, AbstractWeapon> weapons = new ConcurrentHashMap<>();
    private final Map<AbstractWeapon.WeaponType, List<AbstractWeapon>> weaponsByType = new EnumMap<>(AbstractWeapon.WeaponType.class);
    
    public WeaponManager() {
        // Initialize type lists
        for (AbstractWeapon.WeaponType type : AbstractWeapon.WeaponType.values()) {
            weaponsByType.put(type, new ArrayList<>());
        }
    }
    
    /**
     * Register a weapon
     */
    public void registerWeapon(AbstractWeapon weapon) {
        weapons.put(weapon.getName().toLowerCase(), weapon);
        weaponsByType.get(weapon.getType()).add(weapon);
    }
    
    /**
     * Get weapon by name
     */
    public AbstractWeapon getWeapon(String name) {
        return weapons.get(name.toLowerCase());
    }
    
    /**
     * Get all weapons
     */
    public Collection<AbstractWeapon> getAllWeapons() {
        return new ArrayList<>(weapons.values());
    }
    
    /**
     * Get weapons by type
     */
    public List<AbstractWeapon> getWeaponsByType(AbstractWeapon.WeaponType type) {
        return new ArrayList<>(weaponsByType.get(type));
    }
    
    /**
     * Check if weapon exists
     */
    public boolean hasWeapon(String name) {
        return weapons.containsKey(name.toLowerCase());
    }
    
    /**
     * Get weapons player can use
     */
    public List<AbstractWeapon> getAvailableWeapons(Player player) {
        return weapons.values().stream()
                .filter(weapon -> weapon.canUse(player))
                .toList();
    }
    
    /**
     * Get weapons player can afford
     */
    public List<AbstractWeapon> getAffordableWeapons(Player player, int credits) {
        return getAvailableWeapons(player).stream()
                .filter(weapon -> weapon.getCost() <= credits)
                .toList();
    }
    
    /**
     * Get weapons sorted by cost
     */
    public List<AbstractWeapon> getWeaponsSortedByCost(AbstractWeapon.WeaponType type) {
        return weaponsByType.get(type).stream()
                .sorted(Comparator.comparingInt(AbstractWeapon::getCost))
                .toList();
    }
    
    /**
     * Load default weapons
     */
    public void loadDefaultWeapons() {
//        // Sidearms
//        registerWeapon(new ClassicWeapon());
//        registerWeapon(new ShortyWeapon());
//        registerWeapon(new FrenzyWeapon());
//        registerWeapon(new GhostWeapon());
//        registerWeapon(new SheriffWeapon());
//
//        // SMGs
//        registerWeapon(new StingerWeapon());
//        registerWeapon(new SpectreWeapon());
//
//        // Rifles
//        registerWeapon(new BulldogWeapon());
//        registerWeapon(new GuardianWeapon());
//        registerWeapon(new PhantomWeapon());
//        registerWeapon(new VandalWeapon());
//
//        // Snipers
//        registerWeapon(new MarshalWeapon());
//        registerWeapon(new OperatorWeapon());
//
//        // Shotguns
//        registerWeapon(new BuckyWeapon());
//        registerWeapon(new JudgeWeapon());
//
//        // Heavy
//        registerWeapon(new AresWeapon());
//        registerWeapon(new OdinWeapon());
//
//        // Melee
//        registerWeapon(new KnifeWeapon());
        
        dev.ocean.valblock.Plugin.getInstance().getLogger()
                .info("Loaded " + weapons.size() + " default weapons");
    }
    
    /**
     * Unregister a weapon
     */
    public void unregisterWeapon(String name) {
        AbstractWeapon weapon = weapons.remove(name.toLowerCase());
        if (weapon != null) {
            weaponsByType.get(weapon.getType()).remove(weapon);
        }
    }
    
    /**
     * Get weapon statistics
     */
    public WeaponStats getStats() {
        WeaponStats stats = new WeaponStats();
        stats.totalWeapons = weapons.size();
        
        for (AbstractWeapon.WeaponType type : AbstractWeapon.WeaponType.values()) {
            stats.weaponsByType.put(type, weaponsByType.get(type).size());
        }
        
        // Calculate average costs
        for (AbstractWeapon.WeaponType type : AbstractWeapon.WeaponType.values()) {
            List<AbstractWeapon> typeWeapons = weaponsByType.get(type);
            if (!typeWeapons.isEmpty()) {
                double avgCost = typeWeapons.stream()
                        .mapToInt(AbstractWeapon::getCost)
                        .average()
                        .orElse(0.0);
                stats.averageCostByType.put(type, avgCost);
            }
        }
        
        return stats;
    }
    
    @Getter
    public static class WeaponStats {
        private int totalWeapons = 0;
        private Map<AbstractWeapon.WeaponType, Integer> weaponsByType = new EnumMap<>(AbstractWeapon.WeaponType.class);
        private Map<AbstractWeapon.WeaponType, Double> averageCostByType = new EnumMap<>(AbstractWeapon.WeaponType.class);
    }
}