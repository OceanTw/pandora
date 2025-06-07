package dev.ocean.pandora.core.match.impl;

import dev.ocean.pandora.core.arena.Arena;
import dev.ocean.pandora.core.kit.Kit;
import dev.ocean.pandora.core.match.Match;
import dev.ocean.pandora.core.player.User;

import java.util.List;

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
