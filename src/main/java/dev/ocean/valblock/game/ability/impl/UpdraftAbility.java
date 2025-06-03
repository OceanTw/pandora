package dev.ocean.valblock.game.ability.impl;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers.Particle;
import dev.ocean.valblock.game.ability.AbstractAbility;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Collection;

public class UpdraftAbility extends AbstractAbility {

    private static final double LAUNCH_POWER = 1.5;
    private static final int UPDRAFT_DURATION = 3; // seconds
    private static final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

    public UpdraftAbility() {
        super("Updraft", AbilityType.SIGNATURE);
        setDescription("Propel yourself upward");
        setMaxCharges(2);
        setCooldown(20); // 20 second cooldown
        setDuration(UPDRAFT_DURATION);
    }

    @Override
    public boolean execute(Player player) {
        // Launch player upward
        Vector velocity = new Vector(0, LAUNCH_POWER, 0);
        player.setVelocity(velocity);
        
        // Special Minecraft ability - give temporary slow falling
        player.setAllowFlight(true);
        player.setFlying(true);
        
        // Visual effects using packets
        Collection<? extends Player> nearbyPlayers = player.getWorld().getNearbyPlayers(player.getLocation(), 30);
        sendCloudParticles(nearbyPlayers, player.getLocation(), 30);
        
        // Sound effect
        player.getWorld().playSound(player.getLocation(), "valblock.updraft", 1.0f, 1.0f);
        
        player.sendMessage("§9§lUPDRAFT §r§7activated!");
        return true;
    }
    
    private void sendCloudParticles(Collection<? extends Player> players, Location location, int count) {
        // Create cloud particle packet
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.WORLD_PARTICLES);
        packet.getParticles().write(0, Particle.CLOUD);
        packet.getFloat().write(0, (float) location.getX());
        packet.getFloat().write(1, (float) location.getY());
        packet.getFloat().write(2, (float) location.getZ());
        packet.getFloat().write(3, 0.5f);  // offset X
        packet.getFloat().write(4, 0.1f);  // offset Y
        packet.getFloat().write(5, 0.5f);  // offset Z
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
    
    @Override
    public void onEffectEnd(Player player) {
        // End the hovering effect
        if (!player.getGameMode().toString().equals("CREATIVE") && 
            !player.getGameMode().toString().equals("SPECTATOR")) {
            player.setAllowFlight(false);
            player.setFlying(false);
        }
        
        player.sendMessage("§9§lUPDRAFT §r§7expired!");
    }
    
    @Override
    public boolean canUse(Player player) {
        return true;
    }
}