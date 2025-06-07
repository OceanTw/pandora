package dev.ocean.pandora.command;

import dev.ocean.pandora.Pandora;
import dev.ocean.pandora.arena.Arena;
import dev.ocean.pandora.kit.Kit;
import dev.ocean.pandora.player.User;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PandoraCommand implements CommandExecutor {
    private final Pandora plugin;

    public PandoraCommand(Pandora plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;
        User user = plugin.getUserManager().getUser(player.getUniqueId());

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "queue":
                handleQueue(player, user, args);
                break;
            case "leave":
                handleLeave(player, user);
                break;
            case "stats":
                handleStats(player, args);
                break;
            case "spectate":
                handleSpectate(player, args);
                break;
            case "reload":
                handleReload(player);
                break;
            default:
                sendHelp(player);
                break;
        }

        return true;
    }

    private void handleQueue(Player player, User user, String[] args) {
        if (user.getCurrentMatch() != null) {
            player.sendMessage(ChatColor.RED + "You are already in a match!");
            return;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /pandora queue <kit>");
            return;
        }

        Kit kit = plugin.getKitManager().getKit(args[1]);
        if (kit == null) {
            player.sendMessage(ChatColor.RED + "Kit not found!");
            return;
        }

        plugin.getMatchManager().joinQueue(user, kit);
        player.sendMessage(ChatColor.GREEN + "You have joined the queue for " + kit.getDisplayName());

        Arena arena = plugin.getArenaManager().getRandomArena();
        if (arena != null) {
            plugin.getMatchManager().tryMatchmaking(kit, arena);
        }
    }

    private void handleLeave(Player player, User user) {
        if (user.getCurrentMatch() != null) {
            plugin.getMatchManager().endMatch(user.getCurrentMatch().getUuid());
            player.sendMessage(ChatColor.YELLOW + "You have left the match!");
        } else {
            plugin.getMatchManager().leaveQueue(user);
            player.sendMessage(ChatColor.YELLOW + "You have left the queue!");
        }
    }

    private void handleStats(Player player, String[] args) {
        // Implementation for stats command
        player.sendMessage(ChatColor.YELLOW + "Stats feature coming soon!");
    }

    private void handleSpectate(Player player, String[] args) {
        // Implementation for spectate command
        player.sendMessage(ChatColor.YELLOW + "Spectate feature coming soon!");
    }

    private void handleReload(Player player) {
        if (!player.hasPermission("pandora.admin")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return;
        }

        plugin.getConfigManager().reloadConfigs();
        player.sendMessage(ChatColor.GREEN + "Configuration reloaded!");
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Pandora PvP Help ===");
        player.sendMessage(ChatColor.YELLOW + "/pandora queue <kit> - Join a queue");
        player.sendMessage(ChatColor.YELLOW + "/pandora leave - Leave queue/match");
        player.sendMessage(ChatColor.YELLOW + "/pandora stats [player] - View stats");
        player.sendMessage(ChatColor.YELLOW + "/pandora spectate <player> - Spectate a match");
        if (player.hasPermission("pandora.admin")) {
            player.sendMessage(ChatColor.YELLOW + "/pandora reload - Reload configuration");
        }
    }
}