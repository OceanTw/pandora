package dev.ocean.valblock.map;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;

@Data
@RequiredArgsConstructor
public abstract class AbstractMap {
    
    protected final String name;
    protected final String displayName;
    protected final World world;
    protected final String description;
    
    // Spawn locations
    protected List<Location> attackerSpawns;
    protected List<Location> defenderSpawns;
    
    // Site locations
    protected SiteRegion siteA;
    protected SiteRegion siteB;
    protected SiteRegion siteC; // Optional, for maps with 3 sites
    
    // Other important locations
    protected Location midLocation;
    protected List<Location> ultimateOrbLocations;
    protected Set<BuyZone> buyZones;
    
    // Map bounds
    protected Location minBounds;
    protected Location maxBounds;
    
    // Map settings
    protected int roundTimeSeconds = 100;
    protected int buyTimeSeconds = 30;
    protected boolean hasThreeSites = false;
    
    public abstract void onMapLoad();
    public abstract void onMapUnload();
    public abstract void onRoundStart();
    public abstract void onRoundEnd();
    
    /**
     * Check if location is within map bounds
     */
    public boolean isWithinBounds(Location location) {
        if (!location.getWorld().equals(world)) return false;
        
        return location.getX() >= minBounds.getX() && location.getX() <= maxBounds.getX() &&
               location.getY() >= minBounds.getY() && location.getY() <= maxBounds.getY() &&
               location.getZ() >= minBounds.getZ() && location.getZ() <= maxBounds.getZ();
    }
    
    /**
     * Get the closest site to a location
     */
    public SiteRegion getClosestSite(Location location) {
        double distanceA = siteA.getCenter().distance(location);
        double distanceB = siteB.getCenter().distance(location);
        
        if (hasThreeSites && siteC != null) {
            double distanceC = siteC.getCenter().distance(location);
            if (distanceC < distanceA && distanceC < distanceB) {
                return siteC;
            }
        }
        
        return distanceA < distanceB ? siteA : siteB;
    }
    
    /**
     * Check if location is in any buy zone
     */
    public boolean isInBuyZone(Location location, TeamSide team) {
        return buyZones.stream()
                .anyMatch(zone -> zone.getTeam() == team && zone.contains(location));
    }
    
    /**
     * Get random spawn location for team
     */
    public Location getRandomSpawn(TeamSide team) {
        List<Location> spawns = team == TeamSide.ATTACKER ? attackerSpawns : defenderSpawns;
        return spawns.get((int) (Math.random() * spawns.size()));
    }
    
    @Data
    @RequiredArgsConstructor
    public static class SiteRegion {
        private final String name;
        private final Location center;
        private final double radius;
        private final List<Location> spikeSpots;
        
        public boolean contains(Location location) {
            return center.distance(location) <= radius;
        }
        
        public Location getRandomSpikeSpot() {
            return spikeSpots.get((int) (Math.random() * spikeSpots.size()));
        }
    }
    
    @Data
    @RequiredArgsConstructor
    public static class BuyZone {
        private final TeamSide team;
        private final Location corner1;
        private final Location corner2;
        
        public boolean contains(Location location) {
            double minX = Math.min(corner1.getX(), corner2.getX());
            double maxX = Math.max(corner1.getX(), corner2.getX());
            double minY = Math.min(corner1.getY(), corner2.getY());
            double maxY = Math.max(corner1.getY(), corner2.getY());
            double minZ = Math.min(corner1.getZ(), corner2.getZ());
            double maxZ = Math.max(corner1.getZ(), corner2.getZ());
            
            return location.getX() >= minX && location.getX() <= maxX &&
                   location.getY() >= minY && location.getY() <= maxY &&
                   location.getZ() >= minZ && location.getZ() <= maxZ;
        }
    }
    
    public enum TeamSide {
        ATTACKER("§cAttacker", "Attackers must plant the spike"),
        DEFENDER("§9Defender", "Defenders must prevent spike plant or defuse");
        
        private final String displayName;
        private final String description;
        
        TeamSide(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        
        public TeamSide getOpposite() {
            return this == ATTACKER ? DEFENDER : ATTACKER;
        }
    }
}