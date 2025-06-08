package dev.ocean.pandora.listener;

import dev.ocean.pandora.Pandora;
import dev.ocean.pandora.core.match.impl.BoxingMatch;
import dev.ocean.pandora.core.match.impl.SumoMatch;
import dev.ocean.pandora.manager.UserManager;
import dev.ocean.pandora.core.player.User;
import dev.ocean.pandora.core.player.UserStatus;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class MatchListener implements Listener {
    private final Pandora plugin;
    private final UserManager userManager;

    public MatchListener(Pandora plugin) {
        this.plugin = plugin;
        this.userManager = plugin.getUserManager();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        User user = userManager.getUser(player.getUniqueId());

        // Set player status to lobby and give items
        user.setStatus(UserStatus.IN_LOBBY);
        plugin.getLobbyManager().giveItems(player);

        // TODO: Teleport to spawn location when implemented
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        User user = userManager.getUser(player.getUniqueId());

        if (user.getCurrentMatch() != null) {
            plugin.getMatchManager().endMatch(user.getCurrentMatch().getUuid());
        }

        // Remove from queue if in queue
        if (user.getStatus() == UserStatus.IN_QUEUE) {
            plugin.getQueueManager().leaveQueue(user);
        }

        userManager.removeUser(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        User user = userManager.getUser(player.getUniqueId());

        if (user.getCurrentMatch() != null) {
            event.setCancelled(true);
            player.setHealth(20.0);

            // For regular matches, end the match when someone dies
            if (!(user.getCurrentMatch() instanceof BoxingMatch) &&
                    !(user.getCurrentMatch() instanceof SumoMatch)) {
                Player killer = player.getKiller();
                if (killer != null) {
                    plugin.getMatchManager().endMatch(user.getCurrentMatch().getUuid());
                }
            }
        }
    }

    /**
     * Handle general damage events - prevent death in special match types
     */
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        User user = userManager.getUser(player.getUniqueId());

        if (user.getCurrentMatch() != null) {
            // Handle boxing matches - prevent death but allow damage for hit registration
            if (user.getCurrentMatch() instanceof BoxingMatch) {
                // Cancel damage that would kill the player
                if (player.getHealth() - event.getFinalDamage() <= 0) {
                    event.setCancelled(true);
                    player.setHealth(20.0);
                }
            }
            // Handle sumo matches - prevent all damage
            else if (user.getCurrentMatch() instanceof SumoMatch) {
                event.setCancelled(true);
                player.setHealth(20.0);
            }
        }
    }

    /**
     * Handle player vs player damage - special logic for different match types
     */
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) {
            return;
        }

        Player damaged = (Player) event.getEntity();
        Player damager = (Player) event.getDamager();

        User damagedUser = userManager.getUser(damaged.getUniqueId());
        User damagerUser = userManager.getUser(damager.getUniqueId());

        // Check if both players are in the same match
        if (damagedUser.getCurrentMatch() != null &&
                damagerUser.getCurrentMatch() != null &&
                damagedUser.getCurrentMatch().equals(damagerUser.getCurrentMatch())) {

            // Handle boxing matches
            if (damagedUser.getCurrentMatch() instanceof BoxingMatch) {
                BoxingMatch boxingMatch = (BoxingMatch) damagedUser.getCurrentMatch();

                if (boxingMatch.isStarted()) {
                    // Register hit for boxing
                    boxingMatch.registerHit(damagerUser);

                    // Cancel the actual damage but keep the hit registered
                    event.setCancelled(true);
                    damaged.setHealth(20.0);
                } else {
                    // Match hasn't started yet, cancel damage
                    event.setCancelled(true);
                }
            }
            // Handle sumo matches
            else if (damagedUser.getCurrentMatch() instanceof SumoMatch) {
                SumoMatch sumoMatch = (SumoMatch) damagedUser.getCurrentMatch();

                if (sumoMatch.isStarted()) {
                    // Allow knockback but prevent damage
                    event.setDamage(0);
                    damaged.setHealth(20.0);
                } else {
                    // Match hasn't started yet, cancel damage
                    event.setCancelled(true);
                }
            }
            // Handle regular matches (OneVersusOne, etc.)
            else {
                // Allow normal damage for regular matches
                // The death event will handle match ending
            }
        } else {
            // Not in the same match or not in any match - cancel damage
            if (damagedUser.getStatus() != UserStatus.IN_MATCH ||
                    damagerUser.getStatus() != UserStatus.IN_MATCH) {
                event.setCancelled(true);
            }
        }
    }

    /**
     * Handle player movement - special logic for sumo matches
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        // Only check if the player actually moved (not just head movement)
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
                event.getFrom().getBlockY() == event.getTo().getBlockY() &&
                event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        User user = userManager.getUser(player.getUniqueId());

        if (user.getCurrentMatch() instanceof SumoMatch) {
            SumoMatch sumoMatch = (SumoMatch) user.getCurrentMatch();

            if (sumoMatch.isStarted()) {
                // Check if player fell off the platform
                sumoMatch.checkPlayerFall(user);
            }
        }
    }
}