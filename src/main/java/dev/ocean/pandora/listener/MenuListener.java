package dev.ocean.pandora.listener;

import dev.ocean.pandora.Pandora;
import dev.ocean.pandora.core.kit.Kit;
import dev.ocean.pandora.core.player.User;
import dev.ocean.pandora.core.player.UserStatus;
import dev.ocean.pandora.utils.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class MenuListener implements Listener {

    private final Pandora plugin;

    public MenuListener(Pandora plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        if (event.getCurrentItem() == null || event.getCurrentItem().getItemMeta() == null) {
            return;
        }

        String inventoryTitle = event.getView().getTitle();

        // Handle queue menu clicks
        if (inventoryTitle.equals("Unranked Queue") || inventoryTitle.equals("Ranked Queue")) {
            event.setCancelled(true);

            if (event.getCurrentItem().getType() == Material.GRAY_STAINED_GLASS_PANE) {
                return;
            }

            String displayName = event.getCurrentItem().getItemMeta().getDisplayName();
            if (displayName == null) {
                return;
            }

            // Extract kit name from display name (remove colors and formatting)
            String kitDisplayName = ChatColor.stripColor(displayName);
            Kit kit = findKitByDisplayName(kitDisplayName);

            if (kit != null) {
                User user = plugin.getUserManager().getUser(player.getUniqueId());

                if (user.getStatus() != UserStatus.IN_LOBBY) {
                    player.sendMessage(StringUtils.handle("&cYou cannot join a queue right now!"));
                    player.closeInventory();
                    return;
                }

                boolean ranked = inventoryTitle.equals("Ranked Queue");
                joinQueue(player, user, kit, ranked);
                player.closeInventory();
            }
        }
    }

    private Kit findKitByDisplayName(String displayName) {
        return plugin.getKitManager().getAvailableKits().stream()
                .filter(kit -> kit.getDisplayName().equals(displayName))
                .findFirst()
                .orElse(null);
    }

    private void joinQueue(Player player, User user, Kit kit, boolean ranked) {
        plugin.getQueueManager().joinQueue(user, kit, ranked);
    }
}