package dev.ocean.valblock.command;

import dev.ocean.valblock.ability.AbilityType;
import dev.ocean.valblock.ability.AbstractAbility;
import dev.ocean.valblock.manager.AbilityManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ValDebugCommand implements CommandExecutor, TabCompleter {

    private final AbilityManager abilityManager;
    
    public ValDebugCommand(AbilityManager abilityManager) {
        this.abilityManager = abilityManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "ability":
                return handleAbilityCommand(sender, args);
            case "cooldown":
                return handleCooldownCommand(sender, args);
            case "charges":
                return handleChargesCommand(sender, args);
            case "reload":
                return handleReloadCommand(sender);
            case "list":
                return handleListCommand(sender);
            case "reset":
                return handleResetCommand(sender, args);
            case "info":
                return handleInfoCommand(sender, args);
            default:
                sendHelpMessage(sender);
                return true;
        }
    }
    
    private boolean handleAbilityCommand(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "valblock.debug.ability")) return true;
        
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /val ability <player> <ability> [x] [y] [z]");
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found: " + args[1]);
            return true;
        }
        
        String abilityName = args[2];
        AbstractAbility ability = abilityManager.getAbility(abilityName);
        if (ability == null) {
            sender.sendMessage(ChatColor.RED + "Ability not found: " + abilityName);
            return true;
        }
        
        boolean success;
        
        if (args.length >= 6) {
            // Use ability with coordinates
            try {
                double x = Double.parseDouble(args[3]);
                double y = Double.parseDouble(args[4]);
                double z = Double.parseDouble(args[5]);
                success = abilityManager.useAbility(target, abilityName, x, y, z);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid coordinates. Must be numbers.");
                return true;
            }
        } else {
            // Use ability without coordinates
            success = abilityManager.useAbility(target, abilityName);
        }
        
        if (success) {
            sender.sendMessage(ChatColor.GREEN + "Successfully executed " + abilityName + " for " + target.getName());
        } else {
            sender.sendMessage(ChatColor.RED + "Failed to execute ability (on cooldown, no charges, or execution failed)");
        }
        
        return true;
    }
    
    private boolean handleCooldownCommand(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "valblock.debug.cooldown")) return true;
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /val cooldown <get|set|clear> <player> <ability> [seconds]");
            return true;
        }
        
        String operation = args[1].toLowerCase();
        
        if (args.length < 4) {
            sender.sendMessage(ChatColor.RED + "Not enough arguments!");
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[2]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found: " + args[2]);
            return true;
        }
        
        String abilityName = args[3];
        AbstractAbility ability = abilityManager.getAbility(abilityName);
        if (ability == null) {
            sender.sendMessage(ChatColor.RED + "Ability not found: " + abilityName);
            return true;
        }
        
        UUID playerId = target.getUniqueId();
        
        switch (operation) {
            case "get":
                double remaining = abilityManager.getRemainingCooldown(playerId, abilityName);
                sender.sendMessage(ChatColor.YELLOW + target.getName() + "'s " + abilityName + " cooldown: " + 
                        String.format("%.1f", remaining) + " seconds");
                break;
                
            case "set":
                if (args.length < 5) {
                    sender.sendMessage(ChatColor.RED + "Usage: /val cooldown set <player> <ability> <seconds>");
                    return true;
                }
                
                try {
                    double seconds = Double.parseDouble(args[4]);
                    abilityManager.setCooldown(playerId, abilityName, seconds);
                    sender.sendMessage(ChatColor.GREEN + "Set " + target.getName() + "'s " + abilityName + 
                            " cooldown to " + seconds + " seconds");
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid cooldown time. Must be a number.");
                }
                break;
                
            case "clear":
                abilityManager.clearCooldown(playerId, abilityName);
                sender.sendMessage(ChatColor.GREEN + "Cleared " + target.getName() + "'s " + abilityName + " cooldown");
                break;
                
            default:
                sender.sendMessage(ChatColor.RED + "Unknown operation: " + operation);
                sender.sendMessage(ChatColor.RED + "Valid operations: get, set, clear");
        }
        
        return true;
    }
    
    private boolean handleChargesCommand(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "valblock.debug.charges")) return true;
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /val charges <get|set|add> <player> <ability> [amount]");
            return true;
        }
        
        String operation = args[1].toLowerCase();
        
        if (args.length < 4) {
            sender.sendMessage(ChatColor.RED + "Not enough arguments!");
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[2]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found: " + args[2]);
            return true;
        }
        
        String abilityName = args[3];
        AbstractAbility ability = abilityManager.getAbility(abilityName);
        if (ability == null) {
            sender.sendMessage(ChatColor.RED + "Ability not found: " + abilityName);
            return true;
        }
        
        UUID playerId = target.getUniqueId();
        
        switch (operation) {
            case "get":
                int charges = abilityManager.getCharges(playerId, abilityName);
                sender.sendMessage(ChatColor.YELLOW + target.getName() + "'s " + abilityName + " charges: " + 
                        charges + "/" + ability.getMaxCharges());
                break;
                
            case "set":
                if (args.length < 5) {
                    sender.sendMessage(ChatColor.RED + "Usage: /val charges set <player> <ability> <amount>");
                    return true;
                }
                
                try {
                    int amount = Integer.parseInt(args[4]);
                    abilityManager.setCharges(playerId, abilityName, amount);
                    sender.sendMessage(ChatColor.GREEN + "Set " + target.getName() + "'s " + abilityName + 
                            " charges to " + amount);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid amount. Must be a number.");
                }
                break;
                
            case "add":
                if (args.length < 5) {
                    sender.sendMessage(ChatColor.RED + "Usage: /val charges add <player> <ability> <amount>");
                    return true;
                }
                
                try {
                    int amount = Integer.parseInt(args[4]);
                    abilityManager.addCharges(playerId, abilityName, amount);
                    int newCharges = abilityManager.getCharges(playerId, abilityName);
                    sender.sendMessage(ChatColor.GREEN + "Added " + amount + " charges to " + 
                            target.getName() + "'s " + abilityName + ". Now: " + newCharges);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid amount. Must be a number.");
                }
                break;
                
            default:
                sender.sendMessage(ChatColor.RED + "Unknown operation: " + operation);
                sender.sendMessage(ChatColor.RED + "Valid operations: get, set, add");
        }
        
        return true;
    }
    
    private boolean handleReloadCommand(CommandSender sender) {
        if (!checkPermission(sender, "valblock.debug.reload")) return true;
        
        // Clear abilities
        sender.sendMessage(ChatColor.YELLOW + "Reloading abilities...");
        
        // Re-register abilities
        abilityManager.loadDefaultAbilities();
        
        sender.sendMessage(ChatColor.GREEN + "Reloaded " + abilityManager.getAllAbilities().size() + " abilities!");
        return true;
    }
    
    private boolean handleListCommand(CommandSender sender) {
        if (!checkPermission(sender, "valblock.debug.list")) return true;
        
        sender.sendMessage(ChatColor.GREEN + "=== Available Abilities ===");
        
        // Group by type
        for (AbilityType type : AbilityType.values()) {
            List<AbstractAbility> abilities = abilityManager.getAbilitiesByType(type);
            if (!abilities.isEmpty()) {
                sender.sendMessage(ChatColor.YELLOW + "= " + capitalizeFirstLetter(type.name()) + " =");
                for (AbstractAbility ability : abilities) {
                    sender.sendMessage(ChatColor.AQUA + " - " + ability.getName() + 
                            ChatColor.GRAY + " (Cooldown: " + ability.getCooldown() + "s, Charges: " + ability.getMaxCharges() + ")");
                }
            }
        }
        
        return true;
    }
    
    private boolean handleResetCommand(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "valblock.debug.reset")) return true;
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /val reset <player>");
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found: " + args[1]);
            return true;
        }
        
        abilityManager.resetPlayer(target.getUniqueId());
        sender.sendMessage(ChatColor.GREEN + "Reset all ability data for " + target.getName());
        return true;
    }
    
    private boolean handleInfoCommand(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "valblock.debug.info")) return true;
        
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /val info <ability|player> <name>");
            return true;
        }
        
        String infoType = args[1].toLowerCase();
        
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Not enough arguments!");
            return true;
        }
        
        if (infoType.equals("ability")) {
            String abilityName = args[2];
            AbstractAbility ability = abilityManager.getAbility(abilityName);
            if (ability == null) {
                sender.sendMessage(ChatColor.RED + "Ability not found: " + abilityName);
                return true;
            }
            
            sender.sendMessage(ChatColor.GREEN + "=== " + ability.getName() + " ===");
            sender.sendMessage(ChatColor.YELLOW + "Type: " + ability.getType());
            sender.sendMessage(ChatColor.YELLOW + "Description: " + ChatColor.WHITE + ability.getDescription());
            sender.sendMessage(ChatColor.YELLOW + "Cooldown: " + ability.getCooldown() + " seconds");
            sender.sendMessage(ChatColor.YELLOW + "Max Charges: " + ability.getMaxCharges());
            sender.sendMessage(ChatColor.YELLOW + "Duration: " + ability.getDuration() + " seconds");
            
        } else if (infoType.equals("player")) {
            Player target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found: " + args[2]);
                return true;
            }
            
            sender.sendMessage(ChatColor.GREEN + "=== Ability Status for " + target.getName() + " ===");
            
            // List all abilities
            for (AbstractAbility ability : abilityManager.getAllAbilities()) {
                String abilityName = ability.getName();
                UUID playerId = target.getUniqueId();
                
                double cooldown = abilityManager.getRemainingCooldown(playerId, abilityName);
                int charges = abilityManager.getCharges(playerId, abilityName);
                
                String status;
                if (cooldown > 0) {
                    status = ChatColor.RED + String.format("%.1fs", cooldown);
                } else {
                    status = charges > 0 ? 
                            ChatColor.GREEN + "Ready (" + charges + ")" : 
                            ChatColor.YELLOW + "No charges";
                }
                
                sender.sendMessage(ChatColor.AQUA + abilityName + ": " + status);
            }
            
            // Show stats
            AbilityManager.PlayerAbilityStats stats = abilityManager.getPlayerStats(target.getUniqueId());
            sender.sendMessage(ChatColor.YELLOW + "On Cooldown: " + stats.getAbilitiesOnCooldown());
            sender.sendMessage(ChatColor.YELLOW + "Total Charges: " + stats.getTotalCharges());
            sender.sendMessage(ChatColor.YELLOW + "Active Effects: " + stats.getActiveEffects());
            
        } else {
            sender.sendMessage(ChatColor.RED + "Unknown info type: " + infoType);
            sender.sendMessage(ChatColor.RED + "Valid types: ability, player");
        }
        
        return true;
    }
    
    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "=== ValBlock Debug Commands ===");
        sender.sendMessage(ChatColor.YELLOW + "/val ability <player> <ability> [x y z]" + ChatColor.GRAY + " - Execute ability");
        sender.sendMessage(ChatColor.YELLOW + "/val cooldown <get|set|clear> <player> <ability> [seconds]" + ChatColor.GRAY + " - Manage cooldowns");
        sender.sendMessage(ChatColor.YELLOW + "/val charges <get|set|add> <player> <ability> [amount]" + ChatColor.GRAY + " - Manage charges");
        sender.sendMessage(ChatColor.YELLOW + "/val reload" + ChatColor.GRAY + " - Reload abilities");
        sender.sendMessage(ChatColor.YELLOW + "/val list" + ChatColor.GRAY + " - List all abilities");
        sender.sendMessage(ChatColor.YELLOW + "/val reset <player>" + ChatColor.GRAY + " - Reset player ability data");
        sender.sendMessage(ChatColor.YELLOW + "/val info <ability|player> <name>" + ChatColor.GRAY + " - Show detailed info");
    }
    
    private boolean checkPermission(CommandSender sender, String permission) {
        if (sender.hasPermission(permission)) {
            return true;
        }
        sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
        return false;
    }
    
    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.addAll(Arrays.asList("ability", "cooldown", "charges", "reload", "list", "reset", "info"));
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            switch (subCommand) {
                case "ability":
                case "reset":
                    completions.addAll(Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .collect(Collectors.toList()));
                    break;
                case "cooldown":
                    completions.addAll(Arrays.asList("get", "set", "clear"));
                    break;
                case "charges":
                    completions.addAll(Arrays.asList("get", "set", "add"));
                    break;
                case "info":
                    completions.addAll(Arrays.asList("ability", "player"));
                    break;
            }
        } else if (args.length == 3) {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("info") && args[1].equalsIgnoreCase("ability")) {
                // Suggest ability names
                completions.addAll(abilityManager.getAllAbilities().stream()
                        .map(AbstractAbility::getName)
                        .collect(Collectors.toList()));
            } else if (subCommand.equals("info") && args[1].equalsIgnoreCase("player")) {
                // Suggest player names
                completions.addAll(Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .collect(Collectors.toList()));
            } else if (subCommand.equals("cooldown") || subCommand.equals("charges")) {
                // Suggest player names for these subcommands
                completions.addAll(Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .collect(Collectors.toList()));
            } else if (subCommand.equals("ability")) {
                // Suggest ability names
                completions.addAll(abilityManager.getAllAbilities().stream()
                        .map(AbstractAbility::getName)
                        .collect(Collectors.toList()));
            }
        } else if (args.length == 4) {
            String subCommand = args[0].toLowerCase();
            if ((subCommand.equals("cooldown") && !args[1].equals("get")) || 
                (subCommand.equals("charges") && !args[1].equals("get")) || 
                 subCommand.equals("ability")) {
                // Suggest ability names
                completions.addAll(abilityManager.getAllAbilities().stream()
                        .map(AbstractAbility::getName)
                        .collect(Collectors.toList()));
            }
        }
        
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}