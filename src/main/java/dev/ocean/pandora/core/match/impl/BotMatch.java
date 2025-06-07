package dev.ocean.pandora.core.match.impl;

import dev.ocean.pandora.core.arena.Arena;
import dev.ocean.pandora.core.kit.Kit;
import dev.ocean.pandora.core.match.Match;
import dev.ocean.pandora.core.player.User;
import lombok.Getter;

import java.util.List;

@Getter
public class BotMatch extends Match {
    private final String botName;
    private final int botDifficulty;

    public BotMatch(Kit kit, Arena arena, List<User> players, String botName, int botDifficulty) {
        super(kit, arena, players, List.of());
        this.botName = botName;
        this.botDifficulty = botDifficulty;
    }

    @Override
    public void start() {
        teleportPlayers();
        spawnBot();
    }

    @Override
    public void end() {
        removeBot();
    }

    @Override
    public void cleanup() {
        removeBot();
    }

    private void spawnBot() {
        // Future implementation for bot spawning using Citizens or similar NPC plugin
    }

    private void removeBot() {
        // Future implementation for bot removal
    }

}