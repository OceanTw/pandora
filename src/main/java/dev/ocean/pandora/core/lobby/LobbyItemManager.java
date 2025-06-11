package dev.ocean.pandora.core.lobby;

import dev.ocean.pandora.utils.StringUtils;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

@Getter
public class LobbyItemManager {

    private final String joinQueueName = StringUtils.handle("&b&lJoin Queue &7(Right Click)");
    private final String joinRankedQueueName = StringUtils.handle("&6&lJoin Ranked Queue &7(Right Click)");
    private final String createPartyName = StringUtils.handle("&a&lCreate Party &7(Right Click)");
    private final String settingsName = StringUtils.handle("&e&lSettings &7(Right Click)");
    private final String leaveQueueName = StringUtils.handle("&c&lLeave Queue &7(Right Click)");

    public ItemStack getJoinQueueItem() {
        ItemStack item = new ItemStack(Material.IRON_SWORD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(joinQueueName);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack getJoinRankedQueueItem() {
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(joinRankedQueueName);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack getCreatePartyItem() {
        ItemStack item = new ItemStack(Material.NAME_TAG);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(createPartyName);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack getSettingsItem(Player player) {
        // Use PLAYER_HEAD for modern versions
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
        skullMeta.setOwningPlayer(player);
        skullMeta.setDisplayName(settingsName);
        item.setItemMeta(skullMeta);
        return item;
    }

    public ItemStack getLeaveQueueItem() {
        ItemStack item = new ItemStack(Material.REDSTONE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(leaveQueueName);
        item.setItemMeta(meta);
        return item;
    }
}