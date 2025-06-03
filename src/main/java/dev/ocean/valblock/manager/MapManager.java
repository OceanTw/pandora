package dev.ocean.valblock.manager;

import dev.ocean.valblock.map.AbstractMap;
import lombok.Getter;
import org.bukkit.World;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class MapManager {
    
    private final Map<String, AbstractMap> maps = new ConcurrentHashMap<>();
    private final Map<String, AbstractMap> loadedMaps = new ConcurrentHashMap<>();
    
    /**
     * Register a map
     */
    public void registerMap(AbstractMap map) {
        maps.put(map.getName().toLowerCase(), map);
    }
    
    /**
     * Get map by name
     */
    public AbstractMap getMap(String name) {
        return maps.get(name.toLowerCase());
    }
    
    /**
     * Get all maps
     */
    public Collection<AbstractMap> getAllMaps() {
        return new ArrayList<>(maps.values());
    }
    
    /**
     * Check if map exists
     */
    public boolean hasMap(String name) {
        return maps.containsKey(name.toLowerCase());
    }
    
    /**
     * Load a map
     */
    public boolean loadMap(String name) {
        AbstractMap map = maps.get(name.toLowerCase());
        if (map == null) return false;
        
        try {
            map.onMapLoad();
            loadedMaps.put(name.toLowerCase(), map);
            return true;
        } catch (Exception e) {
            dev.ocean.valblock.Plugin.getInstance().getLogger()
                    .severe("Failed to load map " + name + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Unload a map
     */
    public boolean unloadMap(String name) {
        AbstractMap map = loadedMaps.remove(name.toLowerCase());
        if (map == null) return false;
        
        try {
            map.onMapUnload();
            return true;
        } catch (Exception e) {
            dev.ocean.valblock.Plugin.getInstance().getLogger()
                    .severe("Failed to unload map " + name + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get loaded maps
     */
    public Collection<AbstractMap> getLoadedMaps() {
        return new ArrayList<>(loadedMaps.values());
    }
    
    /**
     * Check if map is loaded
     */
    public boolean isMapLoaded(String name) {
        return loadedMaps.containsKey(name.toLowerCase());
    }
    
    /**
     * Get random map
     */
    public AbstractMap getRandomMap() {
        List<AbstractMap> availableMaps = new ArrayList<>(loadedMaps.values());
        if (availableMaps.isEmpty()) {
            // Try to load a random map
            List<AbstractMap> allMaps = new ArrayList<>(maps.values());
            if (!allMaps.isEmpty()) {
                AbstractMap randomMap = allMaps.get(new Random().nextInt(allMaps.size()));
                if (loadMap(randomMap.getName())) {
                    return randomMap;
                }
            }
            return null;
        }
        return availableMaps.get(new Random().nextInt(availableMaps.size()));
    }
    
    /**
     * Get map by world
     */
    public AbstractMap getMapByWorld(World world) {
        return loadedMaps.values().stream()
                .filter(map -> map.getWorld().equals(world))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Load default maps
     */
    public void loadDefaultMaps() {
        // Register all default maps
//        registerMap(new BindMap());
//        registerMap(new HavenMap());
//        registerMap(new SplitMap());
//        registerMap(new AscentMap());
//        registerMap(new DustTwoMap());
//        registerMap(new IceboxMap());
//        registerMap(new BreezeMap());
        
        // Try to load all registered maps
        int loadedCount = 0;
        for (AbstractMap map : maps.values()) {
            if (loadMap(map.getName())) {
                loadedCount++;
            }
        }
        
        dev.ocean.valblock.Plugin.getInstance().getLogger()
                .info("Loaded " + loadedCount + "/" + maps.size() + " maps");
    }
    
    /**
     * Unload all maps
     */
    public void unloadAllMaps() {
        for (String mapName : new ArrayList<>(loadedMaps.keySet())) {
            unloadMap(mapName);
        }
    }
    
    /**
     * Get maps with three sites
     */
    public List<AbstractMap> getThreeSiteMaps() {
        return loadedMaps.values().stream()
                .filter(AbstractMap::isHasThreeSites)
                .toList();
    }
    
    /**
     * Get maps with two sites
     */
    public List<AbstractMap> getTwoSiteMaps() {
        return loadedMaps.values().stream()
                .filter(map -> !map.isHasThreeSites())
                .toList();
    }
    
    /**
     * Get map statistics
     */
    public MapStats getStats() {
        MapStats stats = new MapStats();
        stats.totalMaps = maps.size();
        stats.loadedMaps = loadedMaps.size();
        stats.unloadedMaps = maps.size() - loadedMaps.size();
        
        for (AbstractMap map : loadedMaps.values()) {
            if (map.isHasThreeSites()) {
                stats.threeSiteMaps++;
            } else {
                stats.twoSiteMaps++;
            }
        }
        
        return stats;
    }
    
    @Getter
    public static class MapStats {
        private int totalMaps = 0;
        private int loadedMaps = 0;
        private int unloadedMaps = 0;
        private int twoSiteMaps = 0;
        private int threeSiteMaps = 0;
    }
}