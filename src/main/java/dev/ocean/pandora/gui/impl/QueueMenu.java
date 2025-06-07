package dev.ocean.pandora.gui.impl;

import dev.ocean.pandora.Pandora;
import dev.ocean.pandora.gui.Menu;
import dev.ocean.pandora.core.kit.Kit;
import dev.ocean.pandora.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class QueueMenu extends Menu {

    private final boolean ranked;

    public QueueMenu(boolean ranked) {
        this.ranked = ranked;
    }

    @Override
    public Inventory getInventory(Player player, Object... args) {
        String title = ranked ? "Ranked Queue" : "Unranked Queue";
        Inventory inventory = Bukkit.createInventory(null, 54, title);

        // Fill borders with glass panes
        fillBorders(inventory);

        // Get available kits
        List<Kit> kits = Pandora.getInstance().getKitManager().getAvailableKits();

        // Kit slots (avoiding border slots)
        int[] kitSlots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };

        int kitIndex = 0;
        for (int slot : kitSlots) {
            if (kitIndex < kits.size()) {
                Kit kit = kits.get(kitIndex);
                ItemStack kitItem = createKitItem(kit);
                inventory.setItem(slot, kitItem);
                kitIndex++;
            } else {
                break;
            }
        }

        return inventory;
    }

    private void fillBorders(Inventory inventory) {
        ItemStack borderItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1, (short) 15);
        ItemMeta meta = borderItem.getItemMeta();
        meta.setDisplayName(" ");
        borderItem.setItemMeta(meta);

        // Top and bottom rows
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, borderItem);
            inventory.setItem(i + 45, borderItem);
        }

        // Left and right columns
        for (int i = 9; i < 45; i += 9) {
            inventory.setItem(i, borderItem);
            inventory.setItem(i + 8, borderItem);
        }
    }

    private ItemStack createKitItem(Kit kit) {
        ItemStack item = new ItemStack(kit.getIcon());
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(StringUtils.handle("&b&l" + kit.getDisplayName()));

        List<String> lore = new ArrayList<>();
        lore.add("");

        // Count players in queue for this kit
        int inQueue = countPlayersInQueue(kit);
        int playing = countPlayersPlaying(kit);

        lore.add(StringUtils.handle("&fIn Queue: &b" + inQueue));
        lore.add(StringUtils.handle("&fPlaying: &a" + playing));
        lore.add("");

        if (ranked) {
            lore.add(StringUtils.handle("&6&lRanked Match"));
            lore.add(StringUtils.handle("&7Gain/lose ELO based on performance"));
        } else {
            lore.add(StringUtils.handle("&b&lUnranked Match"));
            lore.add(StringUtils.handle("&7Practice without ELO changes"));
        }

        lore.add("");
        lore.add(StringUtils.handle("&e&lClick to join queue!"));

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    private int countPlayersInQueue(Kit kit) {
        // This would need to be implemented based on your queue system
        // For now, return 0
        return 0;
    }

    private int countPlayersPlaying(Kit kit) {
        // Count players currently in matches with this kit
        return (int) Pandora.getInstance().getMatchManager().getActiveMatches().stream()
                .filter(match -> match.getKit().equals(kit))
                .mapToLong(match -> match.getRed().size() + match.getBlue().size())
                .sum();
    }
}