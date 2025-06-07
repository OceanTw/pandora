package dev.ocean.pandora.listener;

import dev.ocean.pandora.Pandora;
import dev.ocean.pandora.core.player.User;
import dev.ocean.pandora.core.player.UserStatus;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class ItemListener implements Listener {

    private final Pandora plugin;

    public ItemListener(Pandora plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // Allow creative players to interact normally
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        // Only handle right-click actions
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (event.getItem() == null || event.getItem().getItemMeta() == null) {
            return;
        }

        String displayName = event.getItem().getItemMeta().getDisplayName();
        if (displayName == null) {
            return;
        }

        User user = plugin.getUserManager().getUser(player.getUniqueId());

        // Handle lobby items
        if (displayName.equals(plugin.getLobbyManager().getLobbyItemManager().getJoinQueueName())) {
            plugin.getMenuManager().getUnrankedQueueMenu().open(player);
            event.setCancelled(true);
        } else if (displayName.equals(plugin.getLobbyManager().getLobbyItemManager().getJoinRankedQueueName())) {
            plugin.getMenuManager().getRankedQueueMenu().open(player);
            event.setCancelled(true);
        } else if (displayName.equals(plugin.getLobbyManager().getLobbyItemManager().getCreatePartyName())) {
            // TODO: Implement party system
            player.sendMessage("§cParty system coming soon!");
            event.setCancelled(true);
        } else if (displayName.equals(plugin.getLobbyManager().getLobbyItemManager().getSettingsName())) {
            // TODO: Implement settings menu
            player.sendMessage("§cSettings menu coming soon!");
            event.setCancelled(true);
        } else if (displayName.equals(plugin.getLobbyManager().getLobbyItemManager().getLeaveQueueName())) {
            if (user.getStatus() == UserStatus.IN_QUEUE) {
                // TODO: Implement queue leaving
                user.setStatus(UserStatus.IN_LOBBY);
                plugin.getLobbyManager().giveItems(player);
                player.sendMessage("§eYou have left the queue!");
            }
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        // Allow creative players to interact normally
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        User user = plugin.getUserManager().getUser(player.getUniqueId());

        // Prevent inventory interactions for non-match players
        if (user.getStatus() != UserStatus.IN_MATCH && user.getStatus() != UserStatus.KIT_EDITING) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        // Allow creative players to drop items
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        User user = plugin.getUserManager().getUser(player.getUniqueId());

        // Prevent item dropping for non-match players
        if (user.getStatus() != UserStatus.IN_MATCH) {
            event.setCancelled(true);
        }
    }
}