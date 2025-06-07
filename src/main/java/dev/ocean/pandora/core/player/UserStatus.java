package dev.ocean.pandora.core.player;

import lombok.Getter;

@Getter
public enum UserStatus {
    IN_LOBBY("In Lobby"),
    IN_QUEUE("In Queue"),
    IN_MATCH("In Match"),
    SPECTATING("Spectating"),
    KIT_EDITING("Kit Editing");

    private final String displayName;

    UserStatus(String displayName) {
        this.displayName = displayName;
    }
}