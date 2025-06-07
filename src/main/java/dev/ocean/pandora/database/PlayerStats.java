package dev.ocean.pandora.database;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
public class PlayerStats {
    private UUID uuid;
    private String username;
    private int wins;
    private int losses;
    private int kills;
    private int deaths;
    private int elo;
    private int streak;

    public double getKDRatio() {
        return deaths == 0 ? kills : (double) kills / deaths;
    }

    public double getWinRate() {
        int totalGames = wins + losses;
        return totalGames == 0 ? 0 : (double) wins / totalGames * 100;
    }

    public void addWin() {
        wins++;
        streak = Math.max(0, streak) + 1;
    }

    public void addLoss() {
        losses++;
        streak = Math.min(0, streak) - 1;
    }

    public void addKill() {
        kills++;
    }

    public void addDeath() {
        deaths++;
    }

    public void updateElo(int change) {
        elo = Math.max(0, elo + change);
    }
}