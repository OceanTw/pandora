package dev.ocean.pandora.core.match.impl;

import dev.ocean.pandora.Pandora;
import dev.ocean.pandora.core.arena.Arena;
import dev.ocean.pandora.core.kit.Kit;
import dev.ocean.pandora.core.match.Match;
import dev.ocean.pandora.core.player.User;
import dev.ocean.pandora.core.player.UserStatus;
import dev.ocean.pandora.utils.StringUtils;
import lombok.Getter;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

@Getter
public class OneVersusOneMatch extends Match {
    // Boxing constants
    private static final int COUNTDOWN_SECONDS = 5;
    private static final int BOXING_HITS_TO_WIN = 100;

    // Boxing fields
    private final Pandora plugin;
    private int redHits = 0;
    private int blueHits = 0;
    private boolean started = false;
    private BukkitRunnable countdownTask;
    private BukkitRunnable matchTask;

    public OneVersusOneMatch(Kit kit, Arena arena, List<User> red, List<User> blue) {
        super(kit, arena, red, blue);
        this.plugin = Pandora.getInstance();
    }

    @Override
    public void start() {
        // Teleport players
        teleportPlayers();

        // Check if this is a boxing match
        if (isBoxingKit()) {
            startBoxingMatch();
        } else {
            startRegularMatch();
        }
    }

    public boolean isBoxingKit() {
        // TODO: Make it check the rule's kit
        return getKit().getName().equalsIgnoreCase("boxing") ||
                getKit().getName().toLowerCase().contains("boxing");
    }

    private void startBoxingMatch() {
        // Setup boxing players
        setupBoxingPlayers();

        // Start boxing countdown
        startBoxingCountdown();
    }

    private void startRegularMatch() {
        // Give kit items
        giveKitItems();

        // Set game mode
        setGameModes();

        // Notify players
        notifyMatchStart();
    }

    private void setupBoxingPlayers() {
        getAllPlayers().forEach(user -> {
            Player player = user.toPlayer();
            if (player != null) {
                user.setStatus(UserStatus.IN_MATCH);
                player.setGameMode(GameMode.SURVIVAL);
                player.setHealth(20.0);
                player.setFoodLevel(20);
                player.setSaturation(20.0f);

                // Apply boxing kit
                plugin.getKitManager().applyKit(player, getKit().getName());

                // Send initial message
                player.sendMessage(StringUtils.handle("&e&lBOXING MATCH"));
                player.sendMessage(StringUtils.handle("&7First to &c" + BOXING_HITS_TO_WIN + " &7hits wins!"));
            }
        });
    }

    private void startBoxingCountdown() {
        countdownTask = new BukkitRunnable() {
            private int countdown = COUNTDOWN_SECONDS;

            @Override
            public void run() {
                if (countdown <= 0) {
                    started = true;
                    getAllPlayers().forEach(user -> {
                        Player player = user.toPlayer();
                        if (player != null) {
                            player.sendTitle(
                                    StringUtils.handle("&a&lFIGHT!"),
                                    StringUtils.handle("&7Good luck!"),
                                    10, 40, 10
                            );
                            player.sendMessage(StringUtils.handle("&a&lThe match has started!"));
                        }
                    });
                    startBoxingMatchTimer();
                    cancel();
                    return;
                }

                getAllPlayers().forEach(user -> {
                    Player player = user.toPlayer();
                    if (player != null) {
                        player.sendTitle(
                                StringUtils.handle("&e&l" + countdown),
                                StringUtils.handle("&7Get ready to fight!"),
                                0, 25, 0
                        );
                    }
                });

                countdown--;
            }
        };
        countdownTask.runTaskTimer(plugin, 0L, 20L);
    }

    private void startBoxingMatchTimer() {
        matchTask = new BukkitRunnable() {
            private int timeElapsed = 0;
            private final int maxTime = plugin.getConfigManager().getMatchTimeLimit();

            @Override
            public void run() {
                timeElapsed++;

                // Send scoreboard updates every 5 seconds
                if (timeElapsed % 5 == 0) {
                    updateBoxingScoreboard();
                }

                // Check for time limit
                if (maxTime > 0 && timeElapsed >= maxTime) {
                    endBoxingByTimeLimit();
                    cancel();
                }
            }
        };
        matchTask.runTaskTimer(plugin, 20L, 20L);
    }

    public void registerHit(User attacker) {
        if (!isBoxingKit() || !started) return;

        boolean isRed = getRed().contains(attacker);

        if (isRed) {
            redHits++;
            updateBoxingHitCount();

            if (redHits >= BOXING_HITS_TO_WIN) {
                endBoxingWithWinner(getRed(), getBlue(), "Red Team");
            }
        } else {
            blueHits++;
            updateBoxingHitCount();

            if (blueHits >= BOXING_HITS_TO_WIN) {
                endBoxingWithWinner(getBlue(), getRed(), "Blue Team");
            }
        }
    }

    private void updateBoxingHitCount() {
        getAllPlayers().forEach(user -> {
            Player player = user.toPlayer();
            if (player != null) {
                player.sendMessage(StringUtils.handle(
                        "&7Hits: &c" + redHits + " &7vs &9" + blueHits
                ));
            }
        });
    }

    private void updateBoxingScoreboard() {
        getAllPlayers().forEach(user -> {
            Player player = user.toPlayer();
            if (player != null) {
                player.setLevel(Math.max(redHits, blueHits));
                player.setExp((float) Math.max(redHits, blueHits) / BOXING_HITS_TO_WIN);
            }
        });
    }

    private void endBoxingWithWinner(List<User> winners, List<User> losers, String teamName) {
        getAllPlayers().forEach(user -> {
            Player player = user.toPlayer();
            if (player != null) {
                if (winners.contains(user)) {
                    player.sendTitle(
                            StringUtils.handle("&a&lVICTORY!"),
                            StringUtils.handle("&7You won the boxing match!"),
                            10, 60, 20
                    );
                } else {
                    player.sendTitle(
                            StringUtils.handle("&c&lDEFEAT!"),
                            StringUtils.handle("&7Better luck next time!"),
                            10, 60, 20
                    );
                }
                player.sendMessage(StringUtils.handle("&e" + teamName + " &awon the boxing match!"));
            }
        });

        end();
    }

    private void endBoxingByTimeLimit() {
        String winner;
        List<User> winners, losers;

        if (redHits > blueHits) {
            winner = "Red Team";
            winners = getRed();
            losers = getBlue();
        } else if (blueHits > redHits) {
            winner = "Blue Team";
            winners = getBlue();
            losers = getRed();
        } else {
            // Draw
            getAllPlayers().forEach(user -> {
                Player player = user.toPlayer();
                if (player != null) {
                    player.sendTitle(
                            StringUtils.handle("&e&lDRAW!"),
                            StringUtils.handle("&7The match ended in a tie!"),
                            10, 60, 20
                    );
                }
            });
            end();
            return;
        }

        endBoxingWithWinner(winners, losers, winner);
    }

    @Override
    public void end() {
        started = false;

        // Cancel boxing tasks if they exist
        if (countdownTask != null) {
            countdownTask.cancel();
        }
        if (matchTask != null) {
            matchTask.cancel();
        }

        // Notify players based on match type
        if (!isBoxingKit()) {
            notifyMatchEnd();
        }

        // Clear inventories
        clearInventories();

        // Reset game modes
        resetGameModes();

        // Update user statuses
        updateUserStatuses();
    }

    @Override
    public void cleanup() {
        end();
    }

    private void giveKitItems() {
        getAllPlayers().forEach(user -> {
            Player player = user.toPlayer();
            if (player != null) {
                plugin.getKitManager().applyKit(player, getKit().getName());
            }
        });
    }

    private void setGameModes() {
        getAllPlayers().forEach(user -> {
            Player player = user.toPlayer();
            if (player != null) {
                user.setStatus(UserStatus.IN_MATCH);
                player.setGameMode(GameMode.SURVIVAL);
                player.setHealth(20.0);
                player.setFoodLevel(20);
                player.setSaturation(20.0f);
            }
        });
    }

    private void notifyMatchStart() {
        getAllPlayers().forEach(user -> {
            Player player = user.toPlayer();
            if (player != null) {
                player.sendMessage(StringUtils.handle("&aMatch started! Good luck!"));
            }
        });
    }

    private void notifyMatchEnd() {
        getAllPlayers().forEach(user -> {
            Player player = user.toPlayer();
            if (player != null) {
                player.sendMessage(StringUtils.handle("&eMatch ended!"));
            }
        });
    }

    private void clearInventories() {
        getAllPlayers().forEach(user -> {
            Player player = user.toPlayer();
            if (player != null) {
                player.getInventory().clear();
            }
        });
    }

    private void resetGameModes() {
        getAllPlayers().forEach(user -> {
            Player player = user.toPlayer();
            if (player != null) {
                player.setGameMode(GameMode.ADVENTURE);
                player.setHealth(20.0);
                player.setFoodLevel(20);
            }
        });
    }

    private void updateUserStatuses() {
        getAllPlayers().forEach(user -> {
            user.setStatus(UserStatus.IN_LOBBY);
            user.setCurrentMatch(null);

            // Give lobby items
            Player player = user.toPlayer();
            if (player != null) {
                // Teleport to lobby if enabled
                if (plugin.getConfigManager().shouldTeleportAfterMatch()) {
                    // TODO: Teleport to spawn location when implemented
                }

                plugin.getLobbyManager().giveItems(player);
            }
        });
    }

    private List<User> getAllPlayers() {
        List<User> allPlayers = new java.util.ArrayList<>(getRed());
        allPlayers.addAll(getBlue());
        return allPlayers;
    }

    // Boxing-specific getters
    public boolean isStarted() {
        return started;
    }

    public int getRedHits() {
        return redHits;
    }

    public int getBlueHits() {
        return blueHits;
    }
}