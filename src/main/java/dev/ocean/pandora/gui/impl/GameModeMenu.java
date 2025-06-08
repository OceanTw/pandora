package dev.ocean.pandora.gui.impl;

import dev.ocean.pandora.Pandora;
import dev.ocean.pandora.gui.Menu;
import dev.ocean.pandora.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GameModeMenu extends Menu {

    @Override
    public Inventory getInventory(Player player, Object... args) {
        Inventory inventory = Bukkit.createInventory(null, 27, StringUtils.handle("&6&lSelect Game Mode"));

        // Fill borders with glass panes
        fillBorders(inventory);

        // Add game mode options
        addGameModeItems(inventory);

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
            inventory.setItem(i + 18, borderItem);
        }

        // Left and right columns
        inventory.setItem(9, borderItem);
        inventory.setItem(17, borderItem);
    }

    private void addGameModeItems(Inventory inventory) {
        // Unranked Queue
        ItemStack unrankedItem = new ItemStack(Material.IRON_SWORD);
        ItemMeta unrankedMeta = unrankedItem.getItemMeta();
        unrankedMeta.setDisplayName(StringUtils.handle("&b&lUnranked Queue"));
        List<String> unrankedLore = new ArrayList<>();
        unrankedLore.add("");
        unrankedLore.add(StringUtils.handle("&7Play casual matches without"));
        unrankedLore.add(StringUtils.handle("&7affecting your ranking"));
        unrankedLore.add("");
        unrankedLore.add(StringUtils.handle("&a✓ &7Fun and relaxed"));
        unrankedLore.add(StringUtils.handle("&a✓ &7Practice your skills"));
        unrankedLore.add(StringUtils.handle("&a✓ &7All kits available"));
        unrankedLore.add("");
        unrankedLore.add(StringUtils.handle("&e&lClick to enter unranked queue!"));
        unrankedMeta.setLore(unrankedLore);
        unrankedItem.setItemMeta(unrankedMeta);
        inventory.setItem(11, unrankedItem);

        // Ranked Queue
        ItemStack rankedItem = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta rankedMeta = rankedItem.getItemMeta();
        rankedMeta.setDisplayName(StringUtils.handle("&6&lRanked Queue"));
        List<String> rankedLore = new ArrayList<>();
        rankedLore.add("");
        rankedLore.add(StringUtils.handle("&7Competitive matches that"));
        rankedLore.add(StringUtils.handle("&7affect your ELO rating"));
        rankedLore.add("");
        rankedLore.add(StringUtils.handle("&a✓ &7Gain/lose ELO"));
        rankedLore.add(StringUtils.handle("&a✓ &7Skill-based matchmaking"));
        rankedLore.add(StringUtils.handle("&a✓ &7Climb the leaderboard"));
        rankedLore.add("");
        rankedLore.add(StringUtils.handle("&e&lClick to enter ranked queue!"));
        rankedMeta.setLore(rankedLore);
        rankedItem.setItemMeta(rankedMeta);
        inventory.setItem(13, rankedItem);

        // Bot Training
        ItemStack botItem = new ItemStack(Material.ZOMBIE_HEAD);
        ItemMeta botMeta = botItem.getItemMeta();
        botMeta.setDisplayName(StringUtils.handle("&c&lBot Training"));
        List<String> botLore = new ArrayList<>();
        botLore.add("");
        botLore.add(StringUtils.handle("&7Practice against AI bots"));
        botLore.add(StringUtils.handle("&7with different difficulty levels"));
        botLore.add("");
        botLore.add(StringUtils.handle("&a✓ &7Perfect for warming up"));
        botLore.add(StringUtils.handle("&a✓ &7No waiting for players"));
        botLore.add(StringUtils.handle("&a✓ &7Adjustable difficulty"));
        botLore.add("");
        botLore.add(StringUtils.handle("&e&lClick to fight bots!"));
        botMeta.setLore(botLore);
        botItem.setItemMeta(botMeta);
        inventory.setItem(15, botItem);

        // Stats & Profile (完成被截断的部分)
        ItemStack statsItem = new ItemStack(Material.BOOK);
        ItemMeta statsMeta = statsItem.getItemMeta();
        statsMeta.setDisplayName(StringUtils.handle("&d&lStats & Profile"));
        List<String> statsLore = new ArrayList<>();
        statsLore.add("");
        statsLore.add(StringUtils.handle("&7View your statistics"));
        statsLore.add(StringUtils.handle("&7and match history"));
        statsLore.add("");

        // 显示一些基本统计信息
        // TODO: 从数据库获取实际数据
        statsLore.add(StringUtils.handle("&fWins: &a" + "0")); // 这里应该获取实际数据
        statsLore.add(StringUtils.handle("&fLosses: &c" + "0"));
        statsLore.add(StringUtils.handle("&fELO: &6" + "1000"));
        statsLore.add("");
        statsLore.add(StringUtils.handle("&e&lClick to view detailed stats!"));
        statsMeta.setLore(statsLore);
        statsItem.setItemMeta(statsMeta);
        inventory.setItem(10, statsItem); // 放在左下角

        // Kit Editor (新增功能)
        ItemStack kitEditorItem = new ItemStack(Material.CHEST);
        ItemMeta kitEditorMeta = kitEditorItem.getItemMeta();
        kitEditorMeta.setDisplayName(StringUtils.handle("&a&lKit Editor"));
        List<String> kitEditorLore = new ArrayList<>();
        kitEditorLore.add("");
        kitEditorLore.add(StringUtils.handle("&7Customize your loadouts"));
        kitEditorLore.add(StringUtils.handle("&7for different game modes"));
        kitEditorLore.add("");
        kitEditorLore.add(StringUtils.handle("&a✓ &7Save custom layouts"));
        kitEditorLore.add(StringUtils.handle("&a✓ &7Quick access in matches"));
        kitEditorLore.add(StringUtils.handle("&a✓ &7Multiple presets"));
        kitEditorLore.add("");
        kitEditorLore.add(StringUtils.handle("&e&lClick to edit kits!"));
        kitEditorMeta.setLore(kitEditorLore);
        kitEditorItem.setItemMeta(kitEditorMeta);
        inventory.setItem(16, kitEditorItem); // 放在右下角

        // Settings
        ItemStack settingsItem = new ItemStack(Material.REDSTONE);
        ItemMeta settingsMeta = settingsItem.getItemMeta();
        settingsMeta.setDisplayName(StringUtils.handle("&c&lSettings"));
        List<String> settingsLore = new ArrayList<>();
        settingsLore.add("");
        settingsLore.add(StringUtils.handle("&7Configure your preferences"));
        settingsLore.add(StringUtils.handle("&7and game settings"));
        settingsLore.add("");
        settingsLore.add(StringUtils.handle("&a✓ &7Sound settings"));
        settingsLore.add(StringUtils.handle("&a✓ &7Chat preferences"));
        settingsLore.add(StringUtils.handle("&a✓ &7Display options"));
        settingsLore.add("");
        settingsLore.add(StringUtils.handle("&e&lClick to open settings!"));
        settingsMeta.setLore(settingsLore);
        settingsItem.setItemMeta(settingsMeta);
        inventory.setItem(12, settingsItem); // 放在中间下方

        // Leaderboard
        ItemStack leaderboardItem = new ItemStack(Material.GOLDEN_APPLE);
        ItemMeta leaderboardMeta = leaderboardItem.getItemMeta();
        leaderboardMeta.setDisplayName(StringUtils.handle("&e&lLeaderboard"));
        List<String> leaderboardLore = new ArrayList<>();
        leaderboardLore.add("");
        leaderboardLore.add(StringUtils.handle("&7View top players"));
        leaderboardLore.add(StringUtils.handle("&7and rankings"));
        leaderboardLore.add("");
        leaderboardLore.add(StringUtils.handle("&a✓ &7Global ELO rankings"));
        leaderboardLore.add(StringUtils.handle("&a✓ &7Kit-specific stats"));
        leaderboardLore.add(StringUtils.handle("&a✓ &7Monthly competitions"));
        leaderboardLore.add("");
        leaderboardLore.add(StringUtils.handle("&e&lClick to view leaderboard!"));
        leaderboardMeta.setLore(leaderboardLore);
        leaderboardItem.setItemMeta(leaderboardMeta);
        inventory.setItem(14, leaderboardItem); // 放在中间右侧
    }
}