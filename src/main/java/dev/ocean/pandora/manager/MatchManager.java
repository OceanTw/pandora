package dev.ocean.pandora.manager;

import dev.ocean.pandora.core.arena.Arena;
import dev.ocean.pandora.core.kit.Kit;
import dev.ocean.pandora.core.match.Match;
import dev.ocean.pandora.core.match.impl.OneVersusOneMatch;
import dev.ocean.pandora.core.match.impl.BotMatch;
import dev.ocean.pandora.core.player.User;
import lombok.Getter;

import java.util.*;

@Getter
public class MatchManager {
    private final Map<UUID, Match> matches = new HashMap<>();
    private final Map<Kit, Queue<User>> kitQueues = new HashMap<>();

    public Match createMatch(Kit kit, Arena arena, List<User> red, List<User> blue) {
        Match match = new OneVersusOneMatch(kit, arena, red, blue);
        matches.put(match.getUuid(), match);

        red.forEach(user -> user.setCurrentMatch(match));
        blue.forEach(user -> user.setCurrentMatch(match));

        match.start();
        return match;
    }

    public void endMatch(UUID matchId) {
        Match match = matches.remove(matchId);
        if (match != null) {
            match.end();
            match.cleanup();

            getAllUsers(match).forEach(user -> user.setCurrentMatch(null));
        }
    }

    public void joinQueue(User user, Kit kit) {
        Queue<User> queue = kitQueues.computeIfAbsent(kit, k -> new LinkedList<>());
        queue.offer(user);
    }

    public void leaveQueue(User user) {
        kitQueues.values().forEach(queue -> queue.remove(user));
    }

    public boolean tryMatchmaking(Kit kit, Arena arena) {
        Queue<User> queue = kitQueues.get(kit);
        if (queue != null && queue.size() >= 2) {
            User user1 = queue.poll();
            User user2 = queue.poll();

            if (user1 != null && user2 != null) {
                createMatch(kit, arena, Arrays.asList(user1), Arrays.asList(user2));
                return true;
            }
        }
        return false;
    }

    public Match createBotMatch(Kit kit, Arena arena, User player, String botName) {
        BotMatch match = new BotMatch(kit, arena, List.of(player), botName, 1);
        matches.put(match.getUuid(), match);
        player.setCurrentMatch(match);
        match.start();
        return match;
    }

    private List<User> getAllUsers(Match match) {
        List<User> users = new ArrayList<>();
        users.addAll(match.getRed());
        users.addAll(match.getBlue());
        return users;
    }

    public List<Match> getActiveMatches() {
        return new ArrayList<>(matches.values());
    }

    public Match getMatchByPlayer(UUID playerUuid) {
        return matches.values().stream()
                .filter(match -> getAllUsers(match).stream()
                        .anyMatch(user -> user.getUuid().equals(playerUuid)))
                .findFirst()
                .orElse(null);
    }
}