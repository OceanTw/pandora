package dev.ocean.pandora.player;

import dev.ocean.pandora.match.Match;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

@AllArgsConstructor
@Data
public class User {
    private UUID uuid;
    private Match currentMatch;

    public Player toPlayer() {
        return Bukkit.getPlayer(uuid);
    }
}
