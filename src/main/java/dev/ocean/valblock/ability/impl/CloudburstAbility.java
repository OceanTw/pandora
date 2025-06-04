package dev.ocean.valblock.ability.impl;

import dev.ocean.valblock.ability.AbilityType;
import dev.ocean.valblock.ability.AbstractAbility;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;

public class CloudburstAbility extends AbstractAbility {

    private static final double MAX_DISTANCE = 15.0;
    private static final double SMOKE_RADIUS = 3.5;
    private static final int SMOKE_DURATION = 7;
    private static final int PARTICLES_PER_TICK = 50;
    private final Random random = new Random();

    public CloudburstAbility() {
        super(
                "cloudburst",
                "Cloudburst",
                AbilityType.BASIC,
                Material.LIGHT_GRAY_DYE,
                3,
                50,
                0
        );
        setDescription("Throw a projectile that expands into a brief smoke cloud on impact");
        setDuration(SMOKE_DURATION);
    }

    @Override
    public boolean execute(Player player) {
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

        Vector direction = targetLocation.toVector().subtract(startLocation.toVector());
        double distance = Math.min(direction.length(), MAX_DISTANCE);
        direction = direction.normalize().multiply(distance);

        Location smokeLocation = startLocation.clone().add(direction);

        for (double i = 0.5; i < distance; i += 0.5) {
            Location checkLoc = startLocation.clone().add(direction.clone().multiply(i / distance));
            if (!checkLoc.getBlock().isPassable()) {
                smokeLocation = checkLoc;
                break;
            }
        }

        createSmokeCloud(player, smokeLocation);

        player.getWorld().playSound(player.getLocation(), "valblock.cloudburst.throw", 1.0f, 1.0f);
        player.getWorld().playSound(smokeLocation, "valblock.cloudburst.expand", 1.0f, 1.0f);

        player.sendMessage("§b§lCLOUDBURST §r§7deployed!");
        return true;
    }

    private void createSmokeCloud(Player caster, Location center) {
        final long totalTicks = SMOKE_DURATION * 20L;
        final Location finalCenter = center.clone();

        new BukkitRunnable() {
            long ticksElapsed = 0;

            @Override
            public void run() {
                if (ticksElapsed >= totalTicks) {
                    this.cancel();
                    return;
                }

                for (int i = 0; i < PARTICLES_PER_TICK; i++) {
                    double offsetX = (random.nextDouble() * 2 - 1) * SMOKE_RADIUS;
                    double offsetY = (random.nextDouble() * 2 - 1) * SMOKE_RADIUS;
                    double offsetZ = (random.nextDouble() * 2 - 1) * SMOKE_RADIUS;

                    Vector offset = new Vector(offsetX, offsetY, offsetZ);

                    if (offset.length() <= SMOKE_RADIUS) {
                        Location particleLoc = finalCenter.clone().add(offset);
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            player.spawnParticle(Particle.DUST, particleLoc, 0, new Particle.DustOptions(Color.WHITE, 10.0f));
                        }
                    }
                }
                ticksElapsed++;
            }
        }.runTaskTimerAsynchronously(dev.ocean.valblock.Plugin.getInstance(), 0L, 1L);

        registerSmokeZone(center, SMOKE_RADIUS, SMOKE_DURATION);
    }

    private void registerSmokeZone(Location center, double radius, int duration) {
        dev.ocean.valblock.Plugin.getInstance().getServer().getScheduler().runTask(
                dev.ocean.valblock.Plugin.getInstance(),
                () -> {
                    dev.ocean.valblock.Plugin.getInstance().getLogger()
                            .info("Smoke zone created at " + center.getBlockX() + ", " +
                                    center.getBlockY() + ", " + center.getBlockZ() + " for " + duration + " seconds.");
                }
        );
    }

    @Override
    public void onEffectEnd(Player player) {
    }

    @Override
    public boolean canUse(Player player) {
        return true;
    }
}
