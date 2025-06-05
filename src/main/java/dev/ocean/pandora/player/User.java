package dev.ocean.pandora.player;

import dev.ocean.pandora.match.Match;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class User {
    private UUID uuid;
    private Match currentMatch;
}
