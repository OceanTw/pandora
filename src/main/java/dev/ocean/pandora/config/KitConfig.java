package dev.ocean.pandora.config;

import de.exlll.configlib.Configuration;
import de.exlll.configlib.NameFormatters;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Configuration
@Getter
@Setter
public class KitConfig {

    private Map<String, KitData> kits = Map.of();

    @Configuration
    @Getter
    @Setter
    public static class KitData {
        private String displayName = "";
        private String icon = "STICK";
        private List<String> rules = List.of();
        private ItemsData items = new ItemsData();
    }

    @Configuration
    @Getter
    @Setter
    public static class ItemsData {
        private ItemData helmet = null;
        private ItemData chestplate = null;
        private ItemData leggings = null;
        private ItemData boots = null;
        private ItemData sword = null;
        private ItemData bow = null;
        private ItemData weapon = null;
        private List<InventoryItemData> inventory = List.of();
    }

    @Configuration
    @Getter
    @Setter
    public static class ItemData {
        private String material = "STICK";
        private Map<String, Integer> enchantments = Map.of();
        private String color = null;
    }

    @Configuration
    @Getter
    @Setter
    public static class InventoryItemData {
        private String material = "STICK";
        private int amount = 1;
        private Map<String, Integer> enchantments = Map.of();
        private String potionType = null;
        private String color = null;
    }
}