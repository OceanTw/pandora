package dev.ocean.pandora.manager;

import dev.ocean.pandora.arena.Arena;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class ArenaManager {
    private final Map<String, Arena> arenas = new HashMap<>();
    private final Random random = new Random();

    public void addArena(Arena arena) {
        arenas.put(arena.getName(), arena);
    }

    public Arena getArena(String name) {
        return arenas.get(name);
    }

    public Arena getRandomArena() {
        List<Arena> arenaList = arenas.values().stream().toList();
        return arenaList.isEmpty() ? null : arenaList.get(random.nextInt(arenaList.size()));
    }

    public List<Arena> getAvailableArenas() {
        return new ArrayList<>(arenas.values());
    }

    public void removeArena(String name) {
        arenas.remove(name);
    }
}