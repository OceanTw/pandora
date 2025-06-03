package dev.ocean.valblock.agent.impl;

import dev.ocean.valblock.ability.impl.BladeStormAbility;
import dev.ocean.valblock.agent.AbstractAgent;
import dev.ocean.valblock.ability.impl.CloudburstAbility;
import dev.ocean.valblock.ability.impl.TailwindAbility;
import dev.ocean.valblock.ability.impl.UpdraftAbility;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;

public class JettAgent extends AbstractAgent {

    public JettAgent() {
        super(
                "jett",                           // name
                "Jett",                           // displayName
                AgentRole.DUELIST,                // role
                Material.FEATHER,                 // skinMaterial
                "Representing her home country of South Korea, Jett's agile and evasive fighting style lets her take risks no one else can."
        );

        // Set abilities
        this.basicAbility = new CloudburstAbility();
        this.signatureAbility = new UpdraftAbility();
        this.ultimateAbility = new BladeStormAbility();

        // Agent stats
        this.maxHealth = 100;
        this.maxArmor = 50;
        this.movementSpeed = 1.1; // Slightly faster than default

        // Lore for agent selection
        this.lore = Arrays.asList(
                "§7Role: " + role.getDisplayName(),
                "§7Origin: §fSouth Korea",
                "",
                "§7" + description,
                "",
                "§7Abilities:",
                "§f• Basic: §b" + basicAbility.getName(),
                "§f• Signature: §a" + signatureAbility.getName(),
                "§f• Ultimate: §6" + ultimateAbility.getName(),
                "",
                "§7Agent bonuses:",
                "§f• +10% movement speed",
                "§f• Reduced fall damage"
        );
    }

    @Override
    public void onAgentSelect(Player player) {
        player.sendMessage("§a§lAgent Selected: §f" + displayName);
        player.sendMessage("§7" + description);

        // Apply Jett's passive effects
        applyEffects(player);

        // Play selection sound
        player.playSound(player.getLocation(), "valblock.agent.select.jett", 1.0f, 1.0f);
    }

    @Override
    public void onAgentDeselect(Player player) {
        // Remove Jett's effects
        removeEffects(player);

        player.sendMessage("§c§lAgent Deselected: §f" + displayName);
    }

    @Override
    public void onRoundStart(Player player) {
        // Refresh signature ability charges at round start
        // This would be handled by your ability manager

        player.sendMessage("§9Round started as §f" + displayName + "§9!");

        // Give Jett her movement bonus
        applyEffects(player);
    }

    @Override
    public void onRoundEnd(Player player) {
        // Clean up any temporary effects
        removeEffects(player);
    }

    @Override
    public void onDeath(Player player) {
        // Remove effects on death
        removeEffects(player);

        // Death message specific to Jett
        player.sendMessage("§c§lJett down! §7Respawning...");
    }

    @Override
    public void onKill(Player killer, Player victim) {

//        killer.sendMessage("§a§lElimination! §7Speed boost activated!");
        killer.playSound(killer.getLocation(), "valblock.agent.jett.kill", 1.0f, 1.0f);
    }

    @Override
    public void applyEffects(Player player) {
        // Jett's passive: Enhanced mobility
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.SPEED,
                Integer.MAX_VALUE, // Permanent while agent is active
                0, // Speed I (10% boost)
                false,
                false
        ));

        // Reduced fall damage (handled by checking agent in damage events)
        // This would be in your damage listener:
        // if (player has Jett agent) { event.setDamage(event.getDamage() * 0.5); }
    }

    @Override
    public void removeEffects(Player player) {
        // Remove Jett's speed effect
        player.removePotionEffect(PotionEffectType.SPEED);
    }

    @Override
    public boolean canUse(Player player) {
        // Check permissions and any unlock requirements
        return super.canUse(player) &&
                (player.hasPermission("valblock.agent.jett") ||
                        player.hasPermission("valblock.agent.duelist") ||
                        player.hasPermission("valblock.agent.*"));
    }
}