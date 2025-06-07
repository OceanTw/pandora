package dev.ocean.pandora.manager;

import dev.ocean.pandora.Pandora;
import dev.ocean.pandora.config.KitConfig;
import dev.ocean.pandora.core.kit.Kit;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.Color;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class KitManager {
    private final Pandora plugin;
    private final Map<String, Kit> kits = new HashMap<>();

    public KitManager(Pandora plugin) {
        this.plugin = plugin;
    }

    public void loadKitsFromConfig() {
        kits.clear();

        KitConfig config = plugin.getConfigManager().getKitConfig();

        for (Map.Entry<String, KitConfig.KitData> entry : config.getKits().entrySet()) {
            String kitName = entry.getKey();
            KitConfig.KitData data = entry.getValue();

            try {
                Kit kit = createKitFromConfig(kitName, data);
                if (kit != null) {
                    kits.put(kitName, kit);
                    plugin.getLogger().info("Loaded kit: " + kitName);
                } else {
                    plugin.getLogger().warning("Failed to load kit: " + kitName);
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Error loading kit " + kitName + ": " + e.getMessage());
            }
        }

        plugin.getLogger().info("Loaded " + kits.size() + " kits from configuration");
    }

    private Kit createKitFromConfig(String name, KitConfig.KitData data) {
        try {
            Material iconMaterial = Material.valueOf(data.getIcon().toUpperCase());
            Kit kit = new Kit(name, data.getDisplayName(), iconMaterial);

            // Load rules
            for (String rule : data.getRules()) {
                try {
                    Kit.Rules kitRule = Kit.Rules.valueOf(rule.toUpperCase());
                    kit.getEnabledRules().add(kitRule);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid rule '" + rule + "' for kit " + name);
                }
            }

            return kit;
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid icon material '" + data.getIcon() + "' for kit " + name);
            return null;
        }
    }

    public ItemStack[] getKitItems(String kitName) {
        KitConfig config = plugin.getConfigManager().getKitConfig();
        KitConfig.KitData kitData = config.getKits().get(kitName);

        if (kitData == null) {
            return new ItemStack[41]; // Empty inventory
        }

        ItemStack[] items = new ItemStack[41]; // 36 inventory + 4 armor + 1 offhand
        KitConfig.ItemsData itemsData = kitData.getItems();

        // Set armor
        if (itemsData.getHelmet() != null) {
            items[39] = createItemFromConfig(itemsData.getHelmet());
        }
        if (itemsData.getChestplate() != null) {
            items[38] = createItemFromConfig(itemsData.getChestplate());
        }
        if (itemsData.getLeggings() != null) {
            items[37] = createItemFromConfig(itemsData.getLeggings());
        }
        if (itemsData.getBoots() != null) {
            items[36] = createItemFromConfig(itemsData.getBoots());
        }

        // Set main weapons
        if (itemsData.getSword() != null) {
            items[0] = createItemFromConfig(itemsData.getSword());
        }
        if (itemsData.getBow() != null) {
            items[1] = createItemFromConfig(itemsData.getBow());
        }
        if (itemsData.getWeapon() != null) {
            items[0] = createItemFromConfig(itemsData.getWeapon());
        }

        // Set inventory items
        int slot = (itemsData.getSword() != null || itemsData.getWeapon() != null) ?
                (itemsData.getBow() != null ? 2 : 1) : 0;

        for (KitConfig.InventoryItemData invItem : itemsData.getInventory()) {
            if (slot >= 36) break; // Don't exceed inventory slots

            ItemStack item = createInventoryItemFromConfig(invItem);
            if (item != null) {
                items[slot] = item;
                slot++;
            }
        }

        return items;
    }

    private ItemStack createItemFromConfig(KitConfig.ItemData itemData) {
        try {
            Material material = Material.valueOf(itemData.getMaterial().toUpperCase());
            ItemStack item = new ItemStack(material);

            // Apply enchantments
            for (Map.Entry<String, Integer> enchEntry : itemData.getEnchantments().entrySet()) {
                try {
                    Enchantment enchantment = Enchantment.getByKey(
                            org.bukkit.NamespacedKey.minecraft(enchEntry.getKey().toLowerCase())
                    );
                    if (enchantment != null) {
                        item.addUnsafeEnchantment(enchantment, enchEntry.getValue());
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Invalid enchantment: " + enchEntry.getKey());
                }
            }

            // Apply color for leather armor
            if (itemData.getColor() != null && item.getItemMeta() instanceof LeatherArmorMeta) {
                LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
                Color color = parseColor(itemData.getColor());
                if (color != null) {
                    meta.setColor(color);
                    item.setItemMeta(meta);
                }
            }

            return item;
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid material: " + itemData.getMaterial());
            return null;
        }
    }

    private ItemStack createInventoryItemFromConfig(KitConfig.InventoryItemData itemData) {
        try {
            Material material = Material.valueOf(itemData.getMaterial().toUpperCase());
            ItemStack item = new ItemStack(material, itemData.getAmount());

            // Apply enchantments
            for (Map.Entry<String, Integer> enchEntry : itemData.getEnchantments().entrySet()) {
                try {
                    Enchantment enchantment = Enchantment.getByKey(
                            org.bukkit.NamespacedKey.minecraft(enchEntry.getKey().toLowerCase())
                    );
                    if (enchantment != null) {
                        item.addUnsafeEnchantment(enchantment, enchEntry.getValue());
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Invalid enchantment: " + enchEntry.getKey());
                }
            }

            // Apply potion type for splash potions
            if (itemData.getPotionType() != null && item.getItemMeta() instanceof PotionMeta) {
                PotionMeta meta = (PotionMeta) item.getItemMeta();
                try {
                    PotionType potionType = PotionType.valueOf(itemData.getPotionType().toUpperCase());
                    meta.setBasePotionType(potionType);
                    item.setItemMeta(meta);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid potion type: " + itemData.getPotionType());
                }
            }

            // Apply color for leather armor
            if (itemData.getColor() != null && item.getItemMeta() instanceof LeatherArmorMeta) {
                LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
                Color color = parseColor(itemData.getColor());
                if (color != null) {
                    meta.setColor(color);
                    item.setItemMeta(meta);
                }
            }

            return item;
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid material: " + itemData.getMaterial());
            return null;
        }
    }

    private Color parseColor(String colorString) {
        try {
            switch (colorString.toUpperCase()) {
                case "RED": return Color.RED;
                case "BLUE": return Color.BLUE;
                case "GREEN": return Color.GREEN;
                case "YELLOW": return Color.YELLOW;
                case "ORANGE": return Color.ORANGE;
                case "PURPLE": return Color.PURPLE;
                case "BLACK": return Color.BLACK;
                case "WHITE": return Color.WHITE;
                case "GRAY": return Color.GRAY;
                case "LIME": return Color.LIME;
                case "PINK": return Color.FUCHSIA;
                case "AQUA": return Color.AQUA;
                case "NAVY": return Color.NAVY;
                case "SILVER": return Color.SILVER;
                case "MAROON": return Color.MAROON;
                case "OLIVE": return Color.OLIVE;
                case "TEAL": return Color.TEAL;
                default:
                    // Try to parse as RGB hex
                    if (colorString.startsWith("#")) {
                        return Color.fromRGB(Integer.parseInt(colorString.substring(1), 16));
                    }
                    return null;
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Invalid color format: " + colorString);
            return null;
        }
    }

    public void addKit(Kit kit) {
        kits.put(kit.getName(), kit);
    }

    public Kit getKit(String name) {
        return kits.get(name);
    }

    public List<Kit> getAvailableKits() {
        return kits.values().stream().collect(Collectors.toList());
    }

    public List<Kit> getBotKits() {
        return kits.values().stream()
                .filter(kit -> kit.getEnabledRules().contains(Kit.Rules.BOT))
                .collect(Collectors.toList());
    }

    public void removeKit(String name) {
        kits.remove(name);
    }

    public void applyKit(org.bukkit.entity.Player player, String kitName) {
        ItemStack[] items = getKitItems(kitName);
        player.getInventory().clear();
        player.getInventory().setContents(items);
        player.updateInventory();
    }
}