package dev.ocean.valblock;

import dev.ocean.valblock.command.ValDebugCommand;
import dev.ocean.valblock.manager.*;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class Plugin extends JavaPlugin {
    
    @Getter
    private static Plugin instance;
    
    private PlayerManager playerManager;
    private MatchManager matchManager;
    private AgentManager agentManager;
    private WeaponManager weaponManager;
    private MapManager mapManager;
    private AbilityManager abilityManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize managers
        this.playerManager = new PlayerManager();
        this.matchManager = new MatchManager();
        this.agentManager = new AgentManager();
        this.weaponManager = new WeaponManager();
        this.mapManager = new MapManager();
        this.abilityManager = new AbilityManager();
        
        // Register listeners
//        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        
        // Register commands
        // getCommand("valorant").setExecutor(new ValorantCommand());
        getCommand("vdbg").setExecutor(new ValDebugCommand(abilityManager));
        getCommand("vdbg").setTabCompleter(new ValDebugCommand(abilityManager));
        
        // Load default content
        loadDefaultContent();
        
        getLogger().info("ValBlock has been enabled!");
    }
    
    @Override
    public void onDisable() {
        // Clean up active matches
        if (matchManager != null) {
            matchManager.endAllMatches();
        }
        
        getLogger().info("ValBlock has been disabled!");
    }
    
    private void loadDefaultContent() {
        // Load default agents, weapons, maps, and abilities
        agentManager.loadDefaultAgents();
        weaponManager.loadDefaultWeapons();
        mapManager.loadDefaultMaps();
        abilityManager.loadDefaultAbilities();
    }

}