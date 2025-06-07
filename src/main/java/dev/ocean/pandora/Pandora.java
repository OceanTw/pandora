package dev.ocean.pandora;

import dev.ocean.pandora.command.PandoraCommand;
import dev.ocean.pandora.database.DatabaseManager;
import dev.ocean.pandora.listener.MatchListener;
import dev.ocean.pandora.manager.*;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class Pandora extends JavaPlugin {

    @Getter
    private static Pandora instance;

    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private UserManager userManager;
    private MatchManager matchManager;
    private ArenaManager arenaManager;
    private KitManager kitManager;
    private BotManager botManager;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize managers in proper order
        configManager = new ConfigManager(this);
        databaseManager = new DatabaseManager(this, configManager);
        userManager = new UserManager();
        matchManager = new MatchManager();
        arenaManager = new ArenaManager(this);
        kitManager = new KitManager(this);
        botManager = new BotManager();

        // Load configurations and data
        loadArenas();
        loadKits();

        // Register listeners
        getServer().getPluginManager().registerEvents(new MatchListener(this), this);

        // Register commands
        getCommand("pandora").setExecutor(new PandoraCommand(this));

        getLogger().info("Pandora has been enabled!");
    }

    @Override
    public void onDisable() {
        // Save any pending data
        if (configManager != null) {
            configManager.saveConfigs();
        }

        // Disconnect database
        if (databaseManager != null) {
            databaseManager.disconnect();
        }

        // End all active matches
        if (matchManager != null) {
            matchManager.getActiveMatches().forEach(match -> {
                match.end();
                match.cleanup();
            });
        }

        getLogger().info("Pandora has been disabled!");
    }

    private void loadArenas() {
        try {
            arenaManager.loadArenasFromConfig();

            if (arenaManager.getArenas().isEmpty()) {
                getLogger().warning("No arenas loaded! Please configure arenas in arenas.yml");
            }
        } catch (Exception e) {
            getLogger().severe("Failed to load arenas: " + e.getMessage());
        }
    }

    private void loadKits() {
        try {
            kitManager.loadKitsFromConfig();

            if (kitManager.getKits().isEmpty()) {
                getLogger().warning("No kits loaded! Please configure kits in kits.yml");
            }
        } catch (Exception e) {
            getLogger().severe("Failed to load kits: " + e.getMessage());
        }
    }
}