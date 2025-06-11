package dev.ocean.pandora.listener;

import dev.ocean.pandora.Pandora;
import dev.ocean.pandora.core.match.Match;
import dev.ocean.pandora.core.match.impl.OneVersusOneMatch;
import dev.ocean.pandora.core.match.impl.SumoMatch;
import dev.ocean.pandora.manager.UserManager;
import dev.ocean.pandora.core.player.User;
import dev.ocean.pandora.core.player.UserStatus;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
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

        user.setStatus(UserStatus.IN_LOBBY);
        plugin.getLobbyManager().giveItems(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        User user = userManager.getUser(player.getUniqueId());

        if (user.getCurrentMatch() != null) {
            plugin.getMatchManager().endMatch(user.getCurrentMatch().getUuid());
        }

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

            Match match = user.getCurrentMatch();

            if (match instanceof OneVersusOneMatch) {
                OneVersusOneMatch oneVsOneMatch = (OneVersusOneMatch) match;
                if (!oneVsOneMatch.isBoxingKit()) {
                    Player killer = player.getKiller();
                    if (killer != null) {
                        plugin.getMatchManager().endMatch(match.getUuid());
                    }
                }
            } else if (!(match instanceof SumoMatch)) {
                Player killer = player.getKiller();
                if (killer != null) {
                    plugin.getMatchManager().endMatch(match.getUuid());
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) {
            return;
        }

        Player damaged = (Player) event.getEntity();
        Player damager = (Player) event.getDamager();

        User damagedUser = userManager.getUser(damaged.getUniqueId());
        User damagerUser = userManager.getUser(damager.getUniqueId());

        if (damagedUser.getCurrentMatch() != null &&
                damagerUser.getCurrentMatch() != null &&
                damagedUser.getCurrentMatch().equals(damagerUser.getCurrentMatch())) {

            Match match = damagedUser.getCurrentMatch();

            if (match instanceof OneVersusOneMatch) {
                OneVersusOneMatch oneVsOneMatch = (OneVersusOneMatch) match;

                if (oneVsOneMatch.isBoxingKit()) {
                    if (oneVsOneMatch.isStarted()) {
                        oneVsOneMatch.registerHit(damagerUser);
                        damaged.setHealth(20.0);
                    }
                }
            } else if (match instanceof SumoMatch) {
                SumoMatch sumoMatch = (SumoMatch) match;

                if (sumoMatch.isStarted()) {
                    event.setDamage(0);
                    damaged.setHealth(20.0);
                }
            }
        } else {
            if (damagedUser.getStatus() != UserStatus.IN_MATCH ||
                    damagerUser.getStatus() != UserStatus.IN_MATCH) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
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
                sumoMatch.checkPlayerFall(user);
            }
        }
    }
}