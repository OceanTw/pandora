package dev.ocean.valblock.ability.impl;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers.Particle;
import dev.ocean.valblock.ability.AbilityType;
import dev.ocean.valblock.ability.AbstractAbility;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Collection;

public class CloudburstAbility extends AbstractAbility {

    private static final double MAX_DISTANCE = 15.0;
    private static final double SMOKE_RADIUS = 3.5;
    private static final int SMOKE_DURATION = 7; // seconds
    private static final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

    public CloudburstAbility() {
        super(
                "cloudburst",                  // name
                "Cloudburst",                  // displayName
                AbilityType.BASIC,             // type
                Material.LIGHT_GRAY_DYE,       // iconMaterial (smoke theme)
                3,                             // maxCharges
                50,                             // cost (basic ability),
                0
        );
        setDescription("Throw a projectile that expands into a brief smoke cloud on impact");
        setDuration(SMOKE_DURATION);
    }

    @Override
    public boolean execute(Player player) {
        // Default execute to throw in direction player is facing
        Vector direction = player.getLocation().getDirection();
        return execute(player, 
                player.getLocation().getX() + direction.getX() * MAX_DISTANCE,
                player.getLocation().getY() + direction.getY() * MAX_DISTANCE,
                player.getLocation().getZ() + direction.getZ() * MAX_DISTANCE);
    }

    @Override
    public boolean execute(Player player, double x, double y, double z) {
        Location targetLocation = new Location(player.getWorld(), x, y, z);
        Location startLocation = player.getEyeLocation();
        
        // Calculate direction and limit distance
        Vector direction = targetLocation.toVector().subtract(startLocation.toVector());
        double distance = Math.min(direction.length(), MAX_DISTANCE);
        direction = direction.normalize().multiply(distance);
        
        // Calculate final position
        Location smokeLocation = startLocation.clone().add(direction);
        
        // Raycast to find collision
        for (double i = 0.5; i < distance; i += 0.5) {
            Location checkLoc = startLocation.clone().add(direction.clone().multiply(i / distance));
            if (!checkLoc.getBlock().isPassable()) {
                smokeLocation = checkLoc;
                break;
            }
        }
        
        // Create smoke effect
        createSmokeCloud(player, smokeLocation);
        
        // Play sound effect
        player.getWorld().playSound(player.getLocation(), "valblock.cloudburst.throw", 1.0f, 1.0f);
        player.getWorld().playSound(smokeLocation, "valblock.cloudburst.expand", 1.0f, 1.0f);
        
        player.sendMessage("§b§lCLOUDBURST §r§7deployed!");
        return true;
    }

    private void createSmokeCloud(Player caster, Location center) {
        // Get nearby players to send packets to
        Collection<? extends Player> nearbyPlayers = center.getWorld().getNearbyPlayers(center, 50);
        
        // Run expansion effect asynchronously to avoid impacting the main thread
        new BukkitRunnable() {
            @Override
            public void run() {
                // Visual effect - expanding smoke using packets
                for (int i = 0; i < 50; i++) {
                    double radius = SMOKE_RADIUS * Math.min(1.0, i / 10.0);
                    for (int j = 0; j < 8; j++) {
                        double angle = j * Math.PI / 4;
                        double x = radius * Math.cos(angle);
                        double z = radius * Math.sin(angle);
                        
                        Location particleLoc = center.clone().add(x, 0.5, z);
                        sendDustParticle(nearbyPlayers, particleLoc, Color.WHITE, 2.0f, 5);
                    }
                    
                    // Short delay between expansion phases
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.runTaskAsynchronously(dev.ocean.valblock.Plugin.getInstance());
        
        // Register area as smoke zone to block vision (implementation depends on your vision system)
        registerSmokeZone(center, SMOKE_RADIUS, SMOKE_DURATION);
    }
    
    private void sendDustParticle(Collection<? extends Player> players, Location location, Color color, float size, int count) {
        // Create dust particle packet
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.WORLD_PARTICLES);
        packet.getParticles().write(0, Particle.REDSTONE);
        packet.getFloat().write(0, (float) location.getX());
        packet.getFloat().write(1, (float) location.getY());
        packet.getFloat().write(2, (float) location.getZ());
        packet.getFloat().write(3, 0.5f);  // offset X
        packet.getFloat().write(4, 0.5f);  // offset Y
        packet.getFloat().write(5, 0.5f);  // offset Z
        packet.getFloat().write(6, 0.0f);  // particle data (speed)
        packet.getIntegers().write(0, count);  // count
        
        // Set dust color data
        packet.getIntegers().write(1, packColorData(color, size));
        
        // Send to nearby players
        for (Player player : players) {
            try {
                protocolManager.sendServerPacket(player, packet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private int packColorData(Color color, float size) {
        // Convert color and size to packed int format required by dust packets
        return ((int) (size * 255.0F) << 24) | 
               (color.getRed() << 16) | 
               (color.getGreen() << 8) | 
               color.getBlue();
    }
    
    private void registerSmokeZone(Location center, double radius, int duration) {
        // This would use your game's vision system
        dev.ocean.valblock.Plugin.getInstance().getServer().getScheduler().runTask(
            dev.ocean.valblock.Plugin.getInstance(),
            () -> {
                // Register smoke zone in your game system
                // This is a placeholder - implement based on your vision system
                dev.ocean.valblock.Plugin.getInstance().getLogger()
                    .info("Smoke zone created at " + center.getBlockX() + ", " + 
                          center.getBlockY() + ", " + center.getBlockZ());
            }
        );
    }
    
    @Override
    public void onEffectEnd(Player player) {
        // Smoke effect has ended naturally
    }
    
    @Override
    public boolean canUse(Player player) {
        return true;
    }
}