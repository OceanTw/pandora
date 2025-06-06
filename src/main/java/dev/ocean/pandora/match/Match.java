package dev.ocean.pandora.match;

import dev.ocean.pandora.arena.Arena;
import dev.ocean.pandora.kit.Kit;
import dev.ocean.pandora.player.User;

import java.util.List;
import java.util.UUID;

public abstract class Match {
    private final UUID uuid;
    private final Kit kit;
    private final Arena arena;
    private final List<User> red;
    private final List<User> blue;

    public Match(Kit kit, Arena arena, List<User> red, List<User> blue) {
        this.kit = kit;
        this.arena = arena;
        this.red = red;
        this.blue = blue;
        this.uuid = UUID.randomUUID();
    }

    public abstract void start();
    public abstract void end();
    public abstract void cleanup();

    protected void teleportPlayers() {
        red.forEach(it -> it.toPlayer().teleport(arena.getRedSpawn()));
        blue.forEach(it -> it.toPlayer().teleport(arena.getBlueSpawn()));
    }
}
