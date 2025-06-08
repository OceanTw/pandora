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
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

@Getter
public class SumoMatch extends Match {
    private static final int COUNTDOWN_SECONDS = 5;
    private static final int ROUND_TIME_LIMIT = 60; // 60 seconds per round

    private final Pandora plugin;
    private boolean started = false;
    private BukkitRunnable countdownTask;
    private BukkitRunnable matchTask;
    private int roundNumber = 1;
    private static final int MAX_ROUNDS = 3;

    public SumoMatch(Kit kit, Arena arena, List<User> red, List<User> blue) {
        super(kit, arena, red, blue);
        this.plugin = Pandora.getInstance();
    }

    @Override
    public void start() {
        // Teleport players and set up
        teleportPlayers();
        setupPlayers();

        // Start countdown
        startCountdown();
    }

    private void setupPlayers() {
        getAllUsers().forEach(user -> {
            Player player = user.toPlayer();
            if (player != null) {
                user.setStatus(UserStatus.IN_MATCH);
                player.setGameMode(GameMode.SURVIVAL);
                player.setHealth(20.0);
                player.setFoodLevel(20);
                player.setSaturation(20.0f);

                // Apply sumo kit
                plugin.getKitManager().applyKit(player, getKit().getName());

                // Send initial message
                player.sendMessage(StringUtils.handle("&e&lSUMO MATCH"));
                player.sendMessage(StringUtils.handle("&7Push your opponent off the platform!"));
                player.sendMessage(StringUtils.handle("&7Best of " + MAX_ROUNDS + " rounds!"));
            }
        });
    }

    private void startCountdown() {
        countdownTask = new BukkitRunnable() {
            private int countdown = COUNTDOWN_SECONDS;

            @Override
            public void run() {
                if (countdown <= 0) {
                    started = true;
                    getAllUsers().forEach(user -> {
                        Player player = user.toPlayer();
                        if (player != null) {
                            player.sendTitle(
                                    StringUtils.handle("&a&lFIGHT!"),
                                    StringUtils.handle("&7Round " + roundNumber + " - Push them off!"),
                                    10, 40, 10
                            );
                            player.sendMessage(StringUtils.handle("&a&lRound " + roundNumber + " has started!"));
                        }
                    });
                    startRoundTimer();
                    cancel();
                    return;
                }

                getAllUsers().forEach(user -> {
                    Player player = user.toPlayer();
                    if (player != null) {
                        player.sendTitle(
                                StringUtils.handle("&e&l" + countdown),
                                StringUtils.handle("&7Round " + roundNumber + " starting..."),
                                0, 25, 0
                        );
                    }
                });

                countdown--;
            }
        };
        countdownTask.runTaskTimer(plugin, 0L, 20L);
    }

    private void startRoundTimer() {
        matchTask = new BukkitRunnable() {
            private int timeLeft = ROUND_TIME_LIMIT;

            @Override
            public void run() {
                timeLeft--;

                // Update action bar with time
                getAllUsers().forEach(user -> {
                    Player player = user.toPlayer();
                    if (player != null) {
                        player.setLevel(timeLeft);
                        player.setExp((float) timeLeft / ROUND_TIME_LIMIT);
                    }
                });

                // Warn at 10 seconds
                if (timeLeft == 10) {
                    getAllUsers().forEach(user -> {
                        Player player = user.toPlayer();
                        if (player != null) {
                            player.sendMessage(StringUtils.handle("&c&l10 seconds remaining!"));
                        }
                    });
                }

                // Round time limit reached
                if (timeLeft <= 0) {
                    endRoundByTime();
                    cancel();
                }
            }
        };
        matchTask.runTaskTimer(plugin, 20L, 20L);
    }

    public void checkPlayerFall(User user) {
        if (!started) return;

        Player player = user.toPlayer();
        if (player == null) return;

        Location playerLoc = player.getLocation();
        Arena arena = getArena();

        // Check if player fell below the arena bounds
        if (playerLoc.getY() < arena.getMin().getY() - 5 || !isInArenaBounds(playerLoc)) {
            // Player fell off
            boolean playerIsRed = getRed().contains(user);
            List<User> winners = playerIsRed ? getBlue() : getRed();
            List<User> losers = playerIsRed ? getRed() : getBlue();
            String winnerTeam = playerIsRed ? "Blue" : "Red";

            endRound(winners, losers, winnerTeam + " wins round " + roundNumber + "!");
        }
    }

    private boolean isInArenaBounds(Location location) {
        Arena arena = getArena();
        return location.getX() >= arena.getMin().getX() && location.getX() <= arena.getMax().getX() &&
                location.getZ() >= arena.getMin().getZ() && location.getZ() <= arena.getMax().getZ();
    }

    private void endRoundByTime() {
        getAllUsers().forEach(user -> {
            Player player = user.toPlayer();
            if (player != null) {
                player.sendMessage(StringUtils.handle("&e&lRound " + roundNumber + " ended by time limit!"));
            }
        });

        // Check who is closer to center or just call it a draw for this round
        nextRound();
    }

    private void endRound(List<User> winners, List<User> losers, String message) {
        if (matchTask != null) {
            matchTask.cancel();
        }

        getAllUsers().forEach(user -> {
            Player player = user.toPlayer();
            if (player != null) {
                player.sendMessage(StringUtils.handle("&e" + message));
            }
        });

        // Check if match is over
        if (roundNumber >= MAX_ROUNDS) {
            endMatch(winners, losers);
        } else {
            nextRound();
        }
    }

    private void nextRound() {
        roundNumber++;
        started = false;

        // Reset players to spawn positions
        new BukkitRunnable() {
            @Override
            public void run() {
                teleportPlayers();
                setupPlayers();
                startCountdown();
            }
        }.runTaskLater(plugin, 60L); // 3 second delay
    }

    private void endMatch(List<User> winners, List<User> losers) {
        getAllUsers().forEach(user -> {
            Player player = user.toPlayer();
            if (player != null) {
                if (winners.contains(user)) {
                    player.sendTitle(
                            StringUtils.handle("&a&lVICTORY!"),
                            StringUtils.handle("&7You won the sumo match!"),
                            10, 60, 20
                    );
                } else {
                    player.sendTitle(
                            StringUtils.handle("&c&lDEFEAT!"),
                            StringUtils.handle("&7Better luck next time!"),
                            10, 60, 20
                    );
                }
            }
        });

        end();
    }

    @Override
    public void end() {
        started = false;

        // Cancel tasks
        if (countdownTask != null) {
            countdownTask.cancel();
        }
        if (matchTask != null) {
            matchTask.cancel();
        }

        // Reset players
        getAllUsers().forEach(user -> {
            Player player = user.toPlayer();
            if (player != null) {
                user.setStatus(UserStatus.IN_LOBBY);
                player.getInventory().clear();
                player.setGameMode(GameMode.ADVENTURE);

                // Teleport to lobby if enabled
                if (plugin.getConfigManager().shouldTeleportAfterMatch()) {
                    // TODO: Teleport to spawn location when implemented
                }

                // Give lobby items
                plugin.getLobbyManager().giveItems(player);
            }
        });
    }

    @Override
    public void cleanup() {
        end();
    }

    private List<User> getAllUsers() {
        java.util.List<User> users = new java.util.ArrayList<>();
        users.addAll(getRed());
        users.addAll(getBlue());
        return users;
    }

    public boolean isStarted() {
        return started;
    }

    public int getRoundNumber() {
        return roundNumber;
    }
}