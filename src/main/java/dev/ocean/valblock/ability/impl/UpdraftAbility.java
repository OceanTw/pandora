package dev.ocean.valblock.ability.impl;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.particle.type.ParticleTypes;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerParticle;
import dev.ocean.valblock.ability.AbilityType;
import dev.ocean.valblock.ability.AbstractAbility;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Collection;

public class UpdraftAbility extends AbstractAbility {

    private static final double LAUNCH_POWER = 1.5;
    private static final int UPDRAFT_DURATION = 3; // seconds

    public UpdraftAbility() {
        super(
                "updraft",                     // name
                "Updraft",                     // displayName
                AbilityType.BASIC,             // type
                Material.FEATHER,              // iconMaterial (flight theme)
                2,                             // maxCharges
                150,                           // cost
                0                              // cooldown
        );
        setDescription("Propel yourself upward");
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

        // Visual effects using PacketEvents
        Collection<? extends Player> nearbyPlayers = player.getWorld().getNearbyPlayers(player.getLocation(), 30);
        sendCloudParticles(nearbyPlayers, player.getLocation(), 30);

        // Sound effect
        player.getWorld().playSound(player.getLocation(), "valblock.updraft", 1.0f, 1.0f);

        player.sendMessage("§9§lUPDRAFT §r§7activated!");
        return true;
    }

    private void sendCloudParticles(Collection<? extends Player> players, Location location, int count) {
        // Send to nearby players
        for (Player player : players) {
            try {
                player.spawnParticle(Particle.CLOUD, location, count);
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