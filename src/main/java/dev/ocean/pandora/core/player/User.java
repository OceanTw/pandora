package dev.ocean.pandora.core.player;

import dev.ocean.pandora.core.match.Match;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

@AllArgsConstructor
@Data
public class User {
    private UUID uuid;
    private Match currentMatch;
    private UserStatus status;

    public User(UUID uuid, Match currentMatch) {
        this.uuid = uuid;
        this.currentMatch = currentMatch;
        this.status = UserStatus.IN_LOBBY;
    }

    public Player toPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public boolean isInLobby() {
        return status == UserStatus.IN_LOBBY;
    }

    public boolean isInQueue() {
        return status == UserStatus.IN_QUEUE;
    }

    public boolean isInMatch() {
        return status == UserStatus.IN_MATCH;
    }

    public boolean isSpectating() {
        return status == UserStatus.SPECTATING;
    }

    public boolean isEditingKit() {
        return status == UserStatus.KIT_EDITING;
    }
}