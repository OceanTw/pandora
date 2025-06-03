package dev.ocean.valblock.ability;

import lombok.Getter;

@Getter
public enum AbilityType {
    BASIC("§7Basic", "Basic ability"),
    SIGNATURE("§aSignature", "Signature ability - free each round"),
    ULTIMATE("§6Ultimate", "Ultimate ability - requires ultimate points");

    private final String displayName;
    private final String description;

    AbilityType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

}