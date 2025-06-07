package dev.ocean.pandora.manager;

import dev.ocean.pandora.arena.Arena;
import dev.ocean.pandora.kit.Kit;
import dev.ocean.pandora.match.impl.BotMatch;
import dev.ocean.pandora.player.User;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Getter
public class BotManager {
    private final Map<String, BotProfile> botProfiles = new HashMap<>();
    private final Random random = new Random();

    public BotManager() {
        loadDefaultBots();
    }

    private void loadDefaultBots() {
        addBotProfile(new BotProfile("EasyBot", 1, 0.3, 0.5, 0.2));
        addBotProfile(new BotProfile("MediumBot", 2, 0.6, 0.7, 0.4));
        addBotProfile(new BotProfile("HardBot", 3, 0.8, 0.9, 0.6));
        addBotProfile(new BotProfile("ExpertBot", 4, 0.95, 0.95, 0.8));
    }

    public void addBotProfile(BotProfile profile) {
        botProfiles.put(profile.getName(), profile);
    }

    public BotProfile getBotProfile(String name) {
        return botProfiles.get(name);
    }

    public BotProfile getRandomBotProfile() {
        List<BotProfile> profiles = List.copyOf(botProfiles.values());
        return profiles.get(random.nextInt(profiles.size()));
    }

    public BotMatch createBotMatch(Kit kit, Arena arena, User player, String botName) {
        BotProfile profile = getBotProfile(botName);
        if (profile == null) {
            profile = getRandomBotProfile();
        }

        return new BotMatch(kit, arena, List.of(player), profile.getName(), profile.getDifficulty());
    }

    public static class BotProfile {
        @Getter private final String name;
        @Getter private final int difficulty;
        @Getter private final double accuracy;
        @Getter private final double reactionTime;
        @Getter private final double aggression;

        public BotProfile(String name, int difficulty, double accuracy, double reactionTime, double aggression) {
            this.name = name;
            this.difficulty = difficulty;
            this.accuracy = accuracy;
            this.reactionTime = reactionTime;
            this.aggression = aggression;
        }
    }
}