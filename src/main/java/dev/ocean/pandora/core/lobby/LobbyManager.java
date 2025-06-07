package dev.ocean.pandora.core.lobby;

import dev.ocean.pandora.Pandora;
import dev.ocean.pandora.core.player.User;
import org.bukkit.entity.Player;

public class LobbyManager {

    private final Pandora plugin;
    private final LobbyItemManager lobbyItemManager;

    public LobbyManager(Pandora plugin) {
        this.plugin = plugin;
        this.lobbyItemManager = new LobbyItemManager();
    }

    public void giveItems(Player player) {
        User user = plugin.getUserManager().getUser(player.getUniqueId());
        player.getInventory().clear();

        switch (user.getStatus()) {
            case IN_LOBBY:
                giveLobbyItems(player);
                break;
            case IN_QUEUE:
                giveQueueItems(player);
                break;
            case IN_MATCH:
                // Items are handled by match system
                break;
            case SPECTATING:
                giveSpectatingItems(player);
                break;
            case KIT_EDITING:
                // Kit editor handles items
                break;
        }

        player.updateInventory();
    }

    private void giveLobbyItems(Player player) {
        player.getInventory().setItem(0, lobbyItemManager.getJoinQueueItem());
        player.getInventory().setItem(1, lobbyItemManager.getJoinRankedQueueItem());
        player.getInventory().setItem(4, lobbyItemManager.getCreatePartyItem());
        player.getInventory().setItem(8, lobbyItemManager.getSettingsItem(player));
    }

    private void giveQueueItems(Player player) {
        player.getInventory().setItem(8, lobbyItemManager.getLeaveQueueItem());
    }

    private void giveSpectatingItems(Player player) {
        // Add spectating items here when implemented
    }

    public LobbyItemManager getLobbyItemManager() {
        return lobbyItemManager;
    }
}