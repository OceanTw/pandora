package dev.ocean.valblock.ability.impl;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers.Particle;
import dev.ocean.valblock.ability.AbilityType;
import dev.ocean.valblock.ability.AbstractAbility;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Collection;

public class TailwindAbility extends AbstractAbility {

    private static final double DASH_POWER = 2.0;
    private static final double MAX_DISTANCE = 10.0;
    private static final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

    public TailwindAbility() {
        super(
                "tailwind",              // name
                "Tailwind",                    // displayName
                AbilityType.SIGNATURE,         // type
                Material.FEATHER,              // iconMaterial (or whatever material you prefer)
                1,                             // maxCharges
                0,                             // cost (0 since it's a signature ability)
                30
        );
        setDescription("Dash quickly in your movement direction");
        setDuration(0.0); // Instant effect
    }

    @Override
    public boolean execute(Player player) {
        // Get player movement direction
        Vector direction = player.getVelocity();
        
        // If player isn't moving, use looking direction
        if (direction.getX() == 0 && direction.getZ() == 0) {
            direction = player.getLocation().getDirection();
            direction.setY(0);
            direction.normalize();
        } else {
            direction.normalize();
        }
        
        // Apply dash boost
        direction.multiply(DASH_POWER);
        
        // Store start location for effects
        Location startLoc = player.getLocation().clone();
        
        // Apply the dash
        player.setVelocity(direction);
        
        // Create trail effect along dash path
        createDashEffect(player, startLoc, direction);
        
        // Sound effect
        player.getWorld().playSound(player.getLocation(), "valblock.tailwind", 1.0f, 1.0f);
        
        player.sendMessage("§9§lTAILWIND §r§7activated!");
        return true;
    }
    
    private void createDashEffect(Player player, Location startLoc, Vector direction) {
        // Get nearby players to send the effect to
        Collection<? extends Player> nearbyPlayers = player.getWorld().getNearbyPlayers(startLoc, 30);
        
        // Run async to avoid main thread lag
        new BukkitRunnable() {
            @Override
            public void run() {
                // Create trail effect
                Vector normalized = direction.clone().normalize();
                double distance = Math.min(direction.length(), MAX_DISTANCE);
                
                for (double i = 0; i < distance; i += 0.5) {
                    Location particleLoc = startLoc.clone().add(normalized.clone().multiply(i));
                    sendCloudParticle(nearbyPlayers, particleLoc);
                    
                    // Short delay between particles
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.runTaskAsynchronously(dev.ocean.valblock.Plugin.getInstance());
    }
    
    private void sendCloudParticle(Collection<? extends Player> players, Location location) {
        // Create cloud particle packet
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.WORLD_PARTICLES);
        packet.getParticles().write(0, Particle.CLOUD);
        packet.getFloat().write(0, (float) location.getX());
        packet.getFloat().write(1, (float) location.getY());
        packet.getFloat().write(2, (float) location.getZ());
        packet.getFloat().write(3, 0.1f);  // offset X
        packet.getFloat().write(4, 0.1f);  // offset Y
        packet.getFloat().write(5, 0.1f);  // offset Z
        packet.getFloat().write(6, 0.0f);  // particle data (speed)
        packet.getIntegers().write(0, 5);  // count
        
        // Send to nearby players
        for (Player player : players) {
            try {
                protocolManager.sendServerPacket(player, packet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    @Override
    public boolean canUse(Player player) {
        return true;
    }
}