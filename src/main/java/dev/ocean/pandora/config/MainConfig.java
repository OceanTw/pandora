package dev.ocean.pandora.config;

import de.exlll.configlib.Configuration;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Configuration
@Getter
@Setter
public class MainConfig {

    private DatabaseConfig database = new DatabaseConfig();
    private MatchConfig match = new MatchConfig();
    private QueueConfig queue = new QueueConfig();
    private BotConfig bot = new BotConfig();
    private GeneralConfig general = new GeneralConfig();

    @Configuration
    @Getter
    @Setter
    public static class DatabaseConfig {
        private String type = "sqlite";
        private String url = "jdbc:sqlite:plugins/Pandora/database.db";
        private String username = "";
        private String password = "";
    }

    @Configuration
    @Getter
    @Setter
    public static class MatchConfig {
        private int timeLimit = 300;
        private boolean allowSpectating = true;
        private boolean autoStart = true;
        private boolean teleportAfterMatch = true;
    }

    @Configuration
    @Getter
    @Setter
    public static class QueueConfig {
        private int maxQueueTime = 60;
        private boolean enableBotMatches = true;
    }

    @Configuration
    @Getter
    @Setter
    public static class BotConfig {
        private int defaultDifficulty = 2;
        private List<String> names = List.of(
                "PvPBot",
                "TrainingBot",
                "SparringBot",
                "ChallengeBot",
                "DuelBot"
        );
    }

    @Configuration
    @Getter
    @Setter
    public static class GeneralConfig {
        private String prefix = "&8[&6Pandora&8] &r";
        private boolean debug = false;
    }
}