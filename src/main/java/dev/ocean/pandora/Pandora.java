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

        // Initialize managers
        configManager = new ConfigManager(this);
        databaseManager = new DatabaseManager(this, configManager);
        userManager = new UserManager();
        matchManager = new MatchManager();
        arenaManager = new ArenaManager();
        kitManager = new KitManager();
        botManager = new BotManager();

        // Load configurations
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
        if (databaseManager != null) {
            databaseManager.disconnect();
        }

        getLogger().info("Pandora has been disabled!");
    }

    private void loadArenas() {
        // Load arenas from config - implementation depends on your config structure
        getLogger().info("Loaded arenas from configuration");
    }

    private void loadKits() {
        // Load kits from config - implementation depends on your config structure
        getLogger().info("Loaded kits from configuration");
    }
}