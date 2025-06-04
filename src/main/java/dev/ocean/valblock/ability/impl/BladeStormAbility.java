package dev.ocean.valblock.ability.impl;

import dev.ocean.valblock.ability.AbilityType;
import dev.ocean.valblock.ability.AbstractAbility;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
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

    // Track players in Blade Storm mode
    private static final Set<UUID> activeBladeStormPlayers = new HashSet<>();
    // Track original items to restore after ultimate
    private static final java.util.Map<UUID, ItemStack> originalItems = new java.util.HashMap<>();

    public BladeStormAbility() {
        super(
                "bladestorm",                  // name
                "Blade Storm",                 // displayName
                AbilityType.ULTIMATE,          // type
                Material.IRON_SWORD,           // iconMaterial
                1,                             // maxCharges
                100,                            // cost (ultimate ability)
                100
        );
        setDescription("Equip kunai that deal deadly damage at any range");
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

        // Visual effects using player.spawnParticle
        player.spawnParticle(Particle.CRIT, player.getLocation().add(0, 1, 0), 50, 0.3, 0.3, 0.3, 0.1);

        // Sound effect
        player.getWorld().playSound(player.getLocation(), "valblock.bladestorm.activate", 1.0f, 1.0f);
        player.sendMessage("§c§lBLADE STORM §r§7activated!");

        // Setup knife throw listener
        setupKnifeThrowListener(player);

        return true;
    }

    private void setupKnifeThrowListener(Player player) {
        // TODO: listener
        // This method would typically involve registering an event listener
        // that checks for player interactions (e.g., right-clicking with the knives item)
        // and then calls throwKnife().
    }

    public void throwKnife(Player player, Vector direction) {
        // Apply slight spread
        direction = applySpread(direction, KNIFE_SPREAD);

        Location startLoc = player.getEyeLocation();
        Vector normalizedDir = direction.clone().normalize();

        // Perform raycast to find target
        for (double i = 0.5; i <= KNIFE_RANGE; i += 0.5) {
            Location checkLoc = startLoc.clone().add(normalizedDir.clone().multiply(i));

            // Send particle for knife trail using player.spawnParticle
            player.spawnParticle(Particle.CRIT, checkLoc, 1, 0.0, 0.0, 0.0, 0.0);

            // Check for entities at this position
            Collection<Entity> nearbyEntities = player.getWorld().getNearbyEntities(
                    checkLoc, 0.5, 0.5, 0.5);

            for (Entity entity : nearbyEntities) {
                if (entity instanceof LivingEntity && entity != player) {
                    // Hit entity
                    LivingEntity target = (LivingEntity) entity;

                    // Deal damage
                    target.damage(DAMAGE_PER_KNIFE, player);

                    // Effect at hit location using player.spawnParticle
                    player.spawnParticle(Particle.CRIT, target.getLocation().add(0, 1, 0), 20, 0.3, 0.3, 0.3, 0.1);

                    // Play hit sound
                    target.getWorld().playSound(target.getLocation(),
                            "valblock.bladestorm.hit", 1.0f, 1.0f);

                    return;
                }
            }

            // Check for block collision
            if (!checkLoc.getBlock().isPassable()) {
                // Hit effect on block using player.spawnParticle
                player.spawnParticle(Particle.CRIT, checkLoc, 10, 0.3, 0.3, 0.3, 0.1);
                player.getWorld().playSound(checkLoc,
                        "valblock.bladestorm.wall", 0.8f, 1.0f);
                return;
            }
        }

        // If no hit, effect at max range
        Location endLoc = startLoc.clone().add(normalizedDir.multiply(KNIFE_RANGE));
        player.spawnParticle(Particle.CRIT, endLoc, 10, 0.3, 0.3, 0.3, 0.1);
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