package dev.ocean.pandora.config;

import de.exlll.configlib.Configuration;
import de.exlll.configlib.NameFormatters;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Configuration
@Getter
@Setter
public class ArenaConfig {

    private Map<String, ArenaData> arenas = Map.of();

    @Configuration
    @Getter
    @Setter
    public static class ArenaData {
        private String displayName = "";
        private SpawnData redSpawn = new SpawnData();
        private SpawnData blueSpawn = new SpawnData();
        private BoundsData min = new BoundsData();
        private BoundsData max = new BoundsData();
    }

    @Configuration
    @Getter
    @Setter
    public static class SpawnData {
        private String world = "world";
        private double x = 0.0;
        private double y = 64.0;
        private double z = 0.0;
        private float yaw = 0.0f;
        private float pitch = 0.0f;
    }

    @Configuration
    @Getter
    @Setter
    public static class BoundsData {
        private String world = "world";
        private int x = 0;
        private int y = 0;
        private int z = 0;
    }
}