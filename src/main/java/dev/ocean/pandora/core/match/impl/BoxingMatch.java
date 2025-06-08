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
public class BoxingMatch extends Match {
    private static final int COUNTDOWN_SECONDS = 5;
    private static final int BOXING_HITS_TO_WIN = 100;

    private final Pandora plugin;
    private int redHits = 0;
    private int blueHits = 0;
    private boolean started = false;
    private BukkitRunnable countdownTask;
    private BukkitRunnable matchTask;

    public BoxingMatch(Kit kit, Arena arena, List<User> red, List<User> blue) {
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

                // Apply boxing kit
                plugin.getKitManager().applyKit(player, getKit().getName());

                // Send initial message
                player.sendMessage(StringUtils.handle("&e&lBOXING MATCH"));
                player.sendMessage(StringUtils.handle("&7First to &c" + BOXING_HITS_TO_WIN + " &7hits wins!"));
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
                                    StringUtils.handle("&7Good luck!"),
                                    10, 40, 10
                            );
                            player.sendMessage(StringUtils.handle("&a&lThe match has started!"));
                        }
                    });
                    startMatchTimer();
                    cancel();
                    return;
                }

                getAllUsers().forEach(user -> {
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

    private void startMatchTimer() {
        matchTask = new BukkitRunnable() {
            private int timeElapsed = 0;
            private final int maxTime = plugin.getConfigManager().getMatchTimeLimit();

            @Override
            public void run() {
                timeElapsed++;

                // Send scoreboard updates every 5 seconds
                if (timeElapsed % 5 == 0) {
                    updateScoreboard();
                }

                // Check for time limit
                if (maxTime > 0 && timeElapsed >= maxTime) {
                    endByTimeLimit();
                    cancel();
                }
            }
        };
        matchTask.runTaskTimer(plugin, 20L, 20L);
    }

    public void registerHit(User attacker) {
        if (!started) return;

        boolean isRed = getRed().contains(attacker);

        if (isRed) {
            redHits++;
            updateHitCount();

            if (redHits >= BOXING_HITS_TO_WIN) {
                endWithWinner(getRed(), getBlue(), "Red Team");
            }
        } else {
            blueHits++;
            updateHitCount();

            if (blueHits >= BOXING_HITS_TO_WIN) {
                endWithWinner(getBlue(), getRed(), "Blue Team");
            }
        }
    }

    private void updateHitCount() {
        getAllUsers().forEach(user -> {
            Player player = user.toPlayer();
            if (player != null) {
                player.sendMessage(StringUtils.handle(
                        "&7Hits: &c" + redHits + " &7vs &9" + blueHits
                ));
            }
        });
    }

    private void updateScoreboard() {
        // TODO: Implement scoreboard updates
        getAllUsers().forEach(user -> {
            Player player = user.toPlayer();
            if (player != null) {
                player.setLevel(Math.max(redHits, blueHits));
                player.setExp((float) Math.max(redHits, blueHits) / BOXING_HITS_TO_WIN);
            }
        });
    }

    private void endWithWinner(List<User> winners, List<User> losers, String teamName) {
        getAllUsers().forEach(user -> {
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

    private void endByTimeLimit() {
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
            getAllUsers().forEach(user -> {
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

        endWithWinner(winners, losers, winner);
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

    public int getRedHits() {
        return redHits;
    }

    public int getBlueHits() {
        return blueHits;
    }
}