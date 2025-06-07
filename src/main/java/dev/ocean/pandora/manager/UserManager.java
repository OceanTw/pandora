package dev.ocean.pandora.manager;

import dev.ocean.pandora.core.player.User;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class UserManager {
    private final Map<UUID, User> users = new HashMap<>();

    public User getUser(UUID uuid) {
        return users.computeIfAbsent(uuid, k -> new User(uuid, null));
    }

    public void removeUser(UUID uuid) {
        users.remove(uuid);
    }

    public boolean isInMatch(UUID uuid) {
        User user = users.get(uuid);
        return user != null && user.getCurrentMatch() != null;
    }

    public User getUserByName(String name) {
        return users.values().stream()
                .filter(user -> user.toPlayer() != null && user.toPlayer().getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}