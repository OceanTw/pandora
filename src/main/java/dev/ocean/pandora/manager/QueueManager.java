package dev.ocean.pandora.manager;

import dev.ocean.pandora.Pandora;
import dev.ocean.pandora.core.arena.Arena;
import dev.ocean.pandora.core.kit.Kit;
import dev.ocean.pandora.core.player.User;
import dev.ocean.pandora.core.player.UserStatus;
import dev.ocean.pandora.core.queue.Queue;
import dev.ocean.pandora.utils.StringUtils;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class QueueManager {

    private final Pandora plugin;
    private final Map<Kit, List<Queue>> unrankedQueues = new ConcurrentHashMap<>();
    private final Map<Kit, List<Queue>> rankedQueues = new ConcurrentHashMap<>();

    public QueueManager(Pandora plugin) {
        this.plugin = plugin;
    }

    public void joinQueue(User user, Kit kit, boolean ranked) {
        if (user.getStatus() != UserStatus.IN_LOBBY) {
            user.toPlayer().sendMessage(StringUtils.handle("&cYou cannot join a queue right now!"));
            return;
        }

        Map<Kit, List<Queue>> targetQueues = ranked ? rankedQueues : unrankedQueues;
        List<Queue> kitQueues = targetQueues.computeIfAbsent(kit, k -> new ArrayList<>());

        // Create new queue entry
        Queue queue = new Queue(user, kit, ranked, System.currentTimeMillis());
        kitQueues.add(queue);

        // Update user status
        user.setStatus(UserStatus.IN_QUEUE);
        plugin.getLobbyManager().giveItems(user.toPlayer());

        String queueType = ranked ? "ranked" : "unranked";
        user.toPlayer().sendMessage(StringUtils.handle("&aYou have joined the " + queueType + " queue for &b" + kit.getDisplayName() + "&a!"));

        // Try to find a match
        tryMatchmaking(kit, ranked);
    }

    public void leaveQueue(User user) {
        // Remove from all queues
        removeFromQueues(user, unrankedQueues);
        removeFromQueues(user, rankedQueues);

        user.setStatus(UserStatus.IN_LOBBY);
        plugin.getLobbyManager().giveItems(user.toPlayer());
        user.toPlayer().sendMessage(StringUtils.handle("&eYou have left the queue!"));
    }

    private void removeFromQueues(User user, Map<Kit, List<Queue>> queues) {
        for (List<Queue> kitQueues : queues.values()) {
            kitQueues.removeIf(queue -> queue.getUser().equals(user));
        }
    }

    private void tryMatchmaking(Kit kit, boolean ranked) {
        Map<Kit, List<Queue>> targetQueues = ranked ? rankedQueues : unrankedQueues;
        List<Queue> kitQueues = targetQueues.get(kit);

        if (kitQueues == null || kitQueues.size() < 2) {
            return;
        }

        // Get two players from queue
        Queue player1Queue = kitQueues.remove(0);
        Queue player2Queue = kitQueues.remove(0);

        User player1 = player1Queue.getUser();
        User player2 = player2Queue.getUser();

        // Get random arena
        Arena arena = plugin.getArenaManager().getRandomArena();
        if (arena == null) {
            // No arenas available, put players back in queue
            kitQueues.add(0, player1Queue);
            kitQueues.add(1, player2Queue);

            player1.toPlayer().sendMessage(StringUtils.handle("&cNo arenas available! Please try again later."));
            player2.toPlayer().sendMessage(StringUtils.handle("&cNo arenas available! Please try again later."));
            return;
        }

        // Create match
        plugin.getMatchManager().createMatch(kit, arena, Arrays.asList(player1), Arrays.asList(player2));

        // Notify players
        player1.toPlayer().sendMessage(StringUtils.handle("&aMatch found! Teleporting to arena..."));
        player2.toPlayer().sendMessage(StringUtils.handle("&aMatch found! Teleporting to arena..."));
    }

    public int getQueueSize(Kit kit, boolean ranked) {
        Map<Kit, List<Queue>> targetQueues = ranked ? rankedQueues : unrankedQueues;
        List<Queue> kitQueues = targetQueues.get(kit);
        return kitQueues == null ? 0 : kitQueues.size();
    }

    public int getTotalQueueSize() {
        int total = 0;
        for (List<Queue> queues : unrankedQueues.values()) {
            total += queues.size();
        }
        for (List<Queue> queues : rankedQueues.values()) {
            total += queues.size();
        }
        return total;
    }
}