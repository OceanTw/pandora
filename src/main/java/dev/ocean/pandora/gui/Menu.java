package dev.ocean.pandora.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public abstract class Menu {

    public void open(Player player, Object... args) {
        player.openInventory(getInventory(player, args));
    }

    public abstract Inventory getInventory(Player player, Object... args);
}