package dev.ocean.valblock.game.ability.impl;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers.Particle;
import dev.ocean.valblock.game.ability.AbstractAbility;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BladeStormAbility extends AbstractAbility {

    private static final int DURATION = 6; // seconds
    private static final double DAMAGE_PER_KNIFE = 5.0;
    private static final double KNIFE_RANGE = 15.0;
    private static final double KNIFE_SPREAD = 0.2;
    private static final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
    
    // Track players in Blade Storm mode
    private static final Set<UUID> activeBladeStormPlayers = new HashSet<>();
    // Track original items to restore after ultimate
    private static final java.util.Map<UUID, ItemStack> originalItems = new java.util.HashMap<>();

    public BladeStormAbility() {
        super("Blade Storm", AbilityType.ULTIMATE);
        setDescription("Equip kunai that deal deadly damage at any range");
        setMaxCharges(1);
        setCooldown(180); // 3 minutes cooldown (ultimate)
        setDuration(DURATION);
    }

    @Override
    public boolean execute(Player player) {
        // Mark player as in Blade Storm mode
        activeBladeStormPlayers.add(player.getUniqueId());
        
        // Save original item and give blade storm knives
        PlayerInventory inventory = player.getInventory();
        originalItems.put(player.getUniqueId(), inventory.getItemInMainHand().clone());
        
        // Give player special knives item
        ItemStack knives = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = knives.getItemMeta();
        meta.setCustomModelData(1001);
        meta.setDisplayName("§c§lBlade Storm Knives");
        knives.setItemMeta(meta);
        inventory.setItemInMainHand(knives);
        
        // Visual effects with packets
        Collection<? extends Player> nearbyPlayers = player.getWorld().getNearbyPlayers(player.getLocation(), 30);
        sendCritParticles(nearbyPlayers, player.getLocation().add(0, 1, 0), 50);
        
        // Sound effect
        player.getWorld().playSound(player.getLocation(), "valblock.bladestorm.activate", 1.0f, 1.0f);
        player.sendMessage("§c§lBLADE STORM §r§7activated!");
        
        // Setup knife throw listener
        setupKnifeThrowListener(player);
        
        return true;
    }
    
    private void setupKnifeThrowListener(Player player) {
        // TODO: listener
    }
    
    private void throwKnife(Player player, Vector direction) {
        // Apply slight spread
        direction = applySpread(direction, KNIFE_SPREAD);
        
        Location startLoc = player.getEyeLocation();
        Vector normalizedDir = direction.clone().normalize();
        Collection<? extends Player> nearbyPlayers = player.getWorld().getNearbyPlayers(player.getLocation(), 50);
        
        // Perform raycast to find target
        for (double i = 0.5; i <= KNIFE_RANGE; i += 0.5) {
            Location checkLoc = startLoc.clone().add(normalizedDir.clone().multiply(i));
            
            // Send particle packet for knife trail
            sendKnifePathParticle(nearbyPlayers, checkLoc);
            
            // Check for entities at this position
            Collection<Entity> nearbyEntities = player.getWorld().getNearbyEntities(
                checkLoc, 0.5, 0.5, 0.5);
            
            for (Entity entity : nearbyEntities) {
                if (entity instanceof LivingEntity && entity != player) {
                    // Hit entity
                    LivingEntity target = (LivingEntity) entity;
                    
                    // Deal damage
                    target.damage(DAMAGE_PER_KNIFE, player);
                    
                    // Effect at hit location with packets
                    sendCritMagicParticles(nearbyPlayers, target.getLocation().add(0, 1, 0), 20);
                    
                    // Play hit sound
                    target.getWorld().playSound(target.getLocation(), 
                        "valblock.bladestorm.hit", 1.0f, 1.0f);
                    
                    return;
                }
            }
            
            // Check for block collision
            if (!checkLoc.getBlock().isPassable()) {
                // Hit effect on block with packets
                sendCritParticles(nearbyPlayers, checkLoc, 10);
                player.getWorld().playSound(checkLoc, 
                    "valblock.bladestorm.wall", 0.8f, 1.0f);
                return;
            }
        }
        
        // If no hit, effect at max range
        Location endLoc = startLoc.clone().add(normalizedDir.multiply(KNIFE_RANGE));
        sendCritParticles(nearbyPlayers, endLoc, 10);
    }
    
    private Vector applySpread(Vector direction, double spread) {
        // Add slight randomness to direction
        direction.add(new Vector(
            (Math.random() - 0.5) * spread,
            (Math.random() - 0.5) * spread,
            (Math.random() - 0.5) * spread
        ));
        return direction.normalize();
    }
    
    private void sendCritParticles(Collection<? extends Player> players, Location location, int count) {
        // Create crit particle packet
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.WORLD_PARTICLES);
        packet.getParticles().write(0, Particle.CRIT);
        packet.getFloat().write(0, (float) location.getX());
        packet.getFloat().write(1, (float) location.getY());
        packet.getFloat().write(2, (float) location.getZ());
        packet.getFloat().write(3, 0.3f);  // offset X
        packet.getFloat().write(4, 0.3f);  // offset Y
        packet.getFloat().write(5, 0.3f);  // offset Z
        packet.getFloat().write(6, 0.1f);  // particle data (speed)
        packet.getIntegers().write(0, count);  // count
        
        // Send to nearby players
        for (Player player : players) {
            try {
                protocolManager.sendServerPacket(player, packet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private void sendCritMagicParticles(Collection<? extends Player> players, Location location, int count) {
        // Create crit magic particle packet
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.WORLD_PARTICLES);
        packet.getParticles().write(0, Particle.CRIT_MAGIC);
        packet.getFloat().write(0, (float) location.getX());
        packet.getFloat().write(1, (float) location.getY());
        packet.getFloat().write(2, (float) location.getZ());
        packet.getFloat().write(3, 0.3f);  // offset X
        packet.getFloat().write(4, 0.3f);  // offset Y
        packet.getFloat().write(5, 0.3f);  // offset Z
        packet.getFloat().write(6, 0.1f);  // particle data (speed)
        packet.getIntegers().write(0, count);  // count
        
        // Send to nearby players
        for (Player player : players) {
            try {
                protocolManager.sendServerPacket(player, packet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private void sendKnifePathParticle(Collection<? extends Player> players, Location location) {
        // Create crit particle for knife path (single particle, no spread)
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.WORLD_PARTICLES);
        packet.getParticles().write(0, Particle.CRIT);
        packet.getFloat().write(0, (float) location.getX());
        packet.getFloat().write(1, (float) location.getY());
        packet.getFloat().write(2, (float) location.getZ());
        packet.getFloat().write(3, 0.0f);  // offset X
        packet.getFloat().write(4, 0.0f);  // offset Y
        packet.getFloat().write(5, 0.0f);  // offset Z
        packet.getFloat().write(6, 0.0f);  // particle data (speed)
        packet.getIntegers().write(0, 1);  // count
        
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
    public void onEffectEnd(Player player) {
        // End Blade Storm mode
        activeBladeStormPlayers.remove(player.getUniqueId());
        
        // Restore original item
        if (originalItems.containsKey(player.getUniqueId())) {
            player.getInventory().setItemInMainHand(originalItems.remove(player.getUniqueId()));
        }
        
        // Notify player
        player.sendMessage("§c§lBLADE STORM §r§7expired!");
        
        // Sound effect
        player.getWorld().playSound(player.getLocation(), 
            "valblock.bladestorm.end", 1.0f, 1.0f);
    }
    
    @Override
    public boolean canUse(Player player) {
        // Check if player already has Blade Storm active
        return !activeBladeStormPlayers.contains(player.getUniqueId());
    }
}