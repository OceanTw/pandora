package dev.ocean.pandora.listener;

import dev.ocean.pandora.Pandora;
import dev.ocean.pandora.manager.UserManager;
import dev.ocean.pandora.core.player.User;
import dev.ocean.pandora.core.player.UserStatus;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
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

            Player killer = player.getKiller();
            if (killer != null) {
                plugin.getMatchManager().endMatch(user.getCurrentMatch().getUuid());
            }
        }
    }
}