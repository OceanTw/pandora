package dev.ocean.pandora.manager;

import de.exlll.configlib.YamlConfigurationProperties;
import de.exlll.configlib.YamlConfigurations;
import dev.ocean.pandora.Pandora;
import dev.ocean.pandora.config.ArenaConfig;
import dev.ocean.pandora.config.KitConfig;
import dev.ocean.pandora.config.MainConfig;
import lombok.Getter;

import java.nio.file.Path;

@Getter
public class ConfigManager {

    private final Pandora plugin;
    private final YamlConfigurationProperties properties;

    private MainConfig mainConfig;
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

        // Load main configuration
        Path mainPath = plugin.getDataFolder().toPath().resolve("config.yml");
        this.mainConfig = YamlConfigurations.update(mainPath, MainConfig.class, properties);

        // Load arena configuration
        Path arenaPath = plugin.getDataFolder().toPath().resolve("arenas.yml");
        this.arenaConfig = YamlConfigurations.update(arenaPath, ArenaConfig.class, properties);

        // Load kit configuration
        Path kitPath = plugin.getDataFolder().toPath().resolve("kits.yml");
        this.kitConfig = YamlConfigurations.update(kitPath, KitConfig.class, properties);

        plugin.getLogger().info("Successfully loaded all configuration files!");
    }

    public void saveConfigs() {
        try {
            // Save main configuration
            Path mainPath = plugin.getDataFolder().toPath().resolve("config.yml");
            YamlConfigurations.save(mainPath, MainConfig.class, mainConfig, properties);

            // Save arena configuration
            Path arenaPath = plugin.getDataFolder().toPath().resolve("arenas.yml");
            YamlConfigurations.save(arenaPath, ArenaConfig.class, arenaConfig, properties);

            // Save kit configuration
            Path kitPath = plugin.getDataFolder().toPath().resolve("kits.yml");
            YamlConfigurations.save(kitPath, KitConfig.class, kitConfig, properties);

            plugin.getLogger().info("Successfully saved all configuration files!");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to save configuration files: " + e.getMessage());
        }
    }

    public void reloadConfigs() {
        loadConfigs();

        // Reload arenas and kits in their respective managers
        plugin.getArenaManager().loadArenasFromConfig();
        plugin.getKitManager().loadKitsFromConfig();

        plugin.getLogger().info("Configuration reloaded successfully!");
    }

    // Database configuration getters
    public String getDatabaseType() {
        return mainConfig.getDatabase().getType();
    }

    public String getDatabaseUrl() {
        return mainConfig.getDatabase().getUrl();
    }

    public String getDatabaseUsername() {
        return mainConfig.getDatabase().getUsername();
    }

    public String getDatabasePassword() {
        return mainConfig.getDatabase().getPassword();
    }

    // Match configuration getters
    public int getMatchTimeLimit() {
        return mainConfig.getMatch().getTimeLimit();
    }

    public boolean isSpectatingAllowed() {
        return mainConfig.getMatch().isAllowSpectating();
    }

    public boolean isAutoStartEnabled() {
        return mainConfig.getMatch().isAutoStart();
    }

    public boolean shouldTeleportAfterMatch() {
        return mainConfig.getMatch().isTeleportAfterMatch();
    }

    // Queue configuration getters
    public int getMaxQueueTime() {
        return mainConfig.getQueue().getMaxQueueTime();
    }

    public boolean areBotMatchesEnabled() {
        return mainConfig.getQueue().isEnableBotMatches();
    }

    // Bot configuration getters
    public int getDefaultBotDifficulty() {
        return mainConfig.getBot().getDefaultDifficulty();
    }

    public java.util.List<String> getBotNames() {
        return mainConfig.getBot().getNames();
    }

    // General configuration getters
    public String getPrefix() {
        return mainConfig.getGeneral().getPrefix();
    }

    public boolean isDebugEnabled() {
        return mainConfig.getGeneral().isDebug();
    }
}