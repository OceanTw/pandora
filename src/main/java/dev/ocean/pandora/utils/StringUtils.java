package dev.ocean.pandora.utils;

import org.bukkit.ChatColor;

public class StringUtils {

    public static String handle(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String stripColor(String message) {
        return ChatColor.stripColor(message);
    }

    public static String line(String color, int length) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < length; i++) {
            line.append("-");
        }
        return handle(color + "&m" + line.toString());
    }
}