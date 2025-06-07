package dev.ocean.pandora.manager;

import dev.ocean.pandora.Pandora;
import dev.ocean.pandora.core.arena.Arena;
import dev.ocean.pandora.config.ArenaConfig;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.*;

@Getter
public class ArenaManager {
    private final Pandora plugin;
    private final Map<String, Arena> arenas = new HashMap<>();
    private final Random random = new Random();

    public ArenaManager(Pandora plugin) {
        this.plugin = plugin;
    }

    public void loadArenasFromConfig() {
        arenas.clear();

        ArenaConfig config = plugin.getConfigManager().getArenaConfig();

        for (Map.Entry<String, ArenaConfig.ArenaData> entry : config.getArenas().entrySet()) {
            String arenaName = entry.getKey();
            ArenaConfig.ArenaData data = entry.getValue();

            try {
                Arena arena = createArenaFromConfig(arenaName, data);
                if (arena != null) {
                    arenas.put(arenaName, arena);
                    plugin.getLogger().info("Loaded arena: " + arenaName);
                } else {
                    plugin.getLogger().warning("Failed to load arena: " + arenaName + " (invalid world or location)");
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Error loading arena " + arenaName + ": " + e.getMessage());
            }
        }

        plugin.getLogger().info("Loaded " + arenas.size() + " arenas from configuration");
    }

    private Arena createArenaFromConfig(String name, ArenaConfig.ArenaData data) {
        // Get worlds
        World redWorld = Bukkit.getWorld(data.getRedSpawn().getWorld());
        World blueWorld = Bukkit.getWorld(data.getBlueSpawn().getWorld());
        World minWorld = Bukkit.getWorld(data.getMin().getWorld());
        World maxWorld = Bukkit.getWorld(data.getMax().getWorld());

        // Validate worlds exist
        if (redWorld == null || blueWorld == null || minWorld == null || maxWorld == null) {
            plugin.getLogger().warning("One or more worlds not found for arena: " + name);
            return null;
        }

        // Create spawn locations
        Location redSpawn = new Location(
                redWorld,
                data.getRedSpawn().getX(),
                data.getRedSpawn().getY(),
                data.getRedSpawn().getZ(),
                data.getRedSpawn().getYaw(),
                data.getRedSpawn().getPitch()
        );

        Location blueSpawn = new Location(
                blueWorld,
                data.getBlueSpawn().getX(),
                data.getBlueSpawn().getY(),
                data.getBlueSpawn().getZ(),
                data.getBlueSpawn().getYaw(),
                data.getBlueSpawn().getPitch()
        );

        // Create boundary locations
        Location min = new Location(
                minWorld,
                data.getMin().getX(),
                data.getMin().getY(),
                data.getMin().getZ()
        );

        Location max = new Location(
                maxWorld,
                data.getMax().getX(),
                data.getMax().getY(),
                data.getMax().getZ()
        );

        return new Arena(name, data.getDisplayName(), redSpawn, blueSpawn, min, max, 0.0);
    }

    public void addArena(Arena arena) {
        arenas.put(arena.getName(), arena);
    }

    public Arena getArena(String name) {
        return arenas.get(name);
    }

    public Arena getRandomArena() {
        List<Arena> arenaList = new ArrayList<>(arenas.values());
        return arenaList.isEmpty() ? null : arenaList.get(random.nextInt(arenaList.size()));
    }

    public List<Arena> getAvailableArenas() {
        return new ArrayList<>(arenas.values());
    }

    public void removeArena(String name) {
        arenas.remove(name);
    }

    public void saveArenaToConfig(Arena arena) {
        ArenaConfig config = plugin.getConfigManager().getArenaConfig();

        // Create arena data
        ArenaConfig.ArenaData arenaData = new ArenaConfig.ArenaData();
        arenaData.setDisplayName(arena.getDisplayName());

        // Set red spawn
        ArenaConfig.SpawnData redSpawn = new ArenaConfig.SpawnData();
        redSpawn.setWorld(arena.getRedSpawn().getWorld().getName());
        redSpawn.setX(arena.getRedSpawn().getX());
        redSpawn.setY(arena.getRedSpawn().getY());
        redSpawn.setZ(arena.getRedSpawn().getZ());
        redSpawn.setYaw(arena.getRedSpawn().getYaw());
        redSpawn.setPitch(arena.getRedSpawn().getPitch());
        arenaData.setRedSpawn(redSpawn);

        // Set blue spawn
        ArenaConfig.SpawnData blueSpawn = new ArenaConfig.SpawnData();
        blueSpawn.setWorld(arena.getBlueSpawn().getWorld().getName());
        blueSpawn.setX(arena.getBlueSpawn().getX());
        blueSpawn.setY(arena.getBlueSpawn().getY());
        blueSpawn.setZ(arena.getBlueSpawn().getZ());
        blueSpawn.setYaw(arena.getBlueSpawn().getYaw());
        blueSpawn.setPitch(arena.getBlueSpawn().getPitch());
        arenaData.setBlueSpawn(blueSpawn);

        // Set min bounds
        ArenaConfig.BoundsData min = new ArenaConfig.BoundsData();
        min.setWorld(arena.getMin().getWorld().getName());
        min.setX(arena.getMin().getBlockX());
        min.setY(arena.getMin().getBlockY());
        min.setZ(arena.getMin().getBlockZ());
        arenaData.setMin(min);

        // Set max bounds
        ArenaConfig.BoundsData max = new ArenaConfig.BoundsData();
        max.setWorld(arena.getMax().getWorld().getName());
        max.setX(arena.getMax().getBlockX());
        max.setY(arena.getMax().getBlockY());
        max.setZ(arena.getMax().getBlockZ());
        arenaData.setMax(max);

        // Add to config and save
        config.getArenas().put(arena.getName(), arenaData);
        plugin.getConfigManager().saveConfigs();
    }

    public boolean isInArena(Location location, Arena arena) {
        if (!location.getWorld().equals(arena.getMin().getWorld())) {
            return false;
        }

        return location.getX() >= arena.getMin().getX() && location.getX() <= arena.getMax().getX() &&
                location.getY() >= arena.getMin().getY() && location.getY() <= arena.getMax().getY() &&
                location.getZ() >= arena.getMin().getZ() && location.getZ() <= arena.getMax().getZ();
    }
}