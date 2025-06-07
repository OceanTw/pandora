package dev.ocean.pandora.arena;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.Location;

@AllArgsConstructor
@Data
public class Arena {
    private final String name;
    private final String displayName;
    private final Location redSpawn;
    private final Location blueSpawn;
    private final Location min;
    private final Location max;
    private final double buildLimit;
}