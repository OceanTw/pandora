package dev.ocean.pandora.manager;

import de.exlll.configlib.YamlConfigurationProperties;
import de.exlll.configlib.YamlConfigurations;
import dev.ocean.pandora.Pandora;
import dev.ocean.pandora.config.ArenaConfig;
import dev.ocean.pandora.config.KitConfig;
import lombok.Getter;

import java.nio.file.Path;

@Getter
public class ConfigManager {

    private final Pandora plugin;
    private final YamlConfigurationProperties properties;

    private ArenaConfig arenaConfig;
    private KitConfig kitConfig;

    public ConfigManager(Pandora plugin) {
        this.plugin = plugin;
        this.properties = YamlConfigurationProperties.newBuilder()
                .header("Pandora PvP Practice Plugin Configuration")
                .build();

        loadConfigs();
    }

    public void loadConfigs() {
        // Ensure data folder exists
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        // Load arena configuration
        Path arenaPath = plugin.getDataFolder().toPath().resolve("arenas.yml");
        this.arenaConfig = YamlConfigurations.update(arenaPath, ArenaConfig.class, properties);

        // Load kit configuration
        Path kitPath = plugin.getDataFolder().toPath().resolve("kits.yml");
        this.kitConfig = YamlConfigurations.update(kitPath, KitConfig.class, properties);
    }

    public void saveConfigs() {
        // Save arena configuration
        Path arenaPath = plugin.getDataFolder().toPath().resolve("arenas.yml");
        YamlConfigurations.save(arenaPath, ArenaConfig.class, arenaConfig, properties);

        // Save kit configuration
        Path kitPath = plugin.getDataFolder().toPath().resolve("kits.yml");
        YamlConfigurations.save(kitPath, KitConfig.class, kitConfig, properties);
    }

    public void reloadConfigs() {
        loadConfigs();
    }
}