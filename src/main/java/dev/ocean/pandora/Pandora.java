package dev.ocean.pandora;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class Pandora extends JavaPlugin {
    
    @Getter
    private static Pandora instance;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize managers
        
        // Register listeners

        // Register commands

        getLogger().info("ValBlock has been enabled!");
    }
    
    @Override
    public void onDisable() {
        
        getLogger().info("ValBlock has been disabled!");
    }

}