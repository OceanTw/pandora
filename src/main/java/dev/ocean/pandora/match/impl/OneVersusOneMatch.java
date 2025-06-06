package dev.ocean.pandora.match.impl;

import dev.ocean.pandora.arena.Arena;
import dev.ocean.pandora.kit.Kit;
import dev.ocean.pandora.match.Match;
import dev.ocean.pandora.player.User;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.UUID;

public class OneVersusOneMatch extends Match {
    public OneVersusOneMatch(Kit kit, Arena arena, List<User> red, List<User> blue) {
        super(kit, arena, red, blue);
    }

    @Override
    public void start() {
        teleportPlayers();
    }

    @Override
    public void end() {

    }

    @Override
    public void cleanup() {

    }
}
