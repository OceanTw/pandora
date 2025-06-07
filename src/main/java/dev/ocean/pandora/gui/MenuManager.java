package dev.ocean.pandora.gui;

import dev.ocean.pandora.gui.impl.QueueMenu;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class MenuManager {

    private final QueueMenu unrankedQueueMenu;
    private final QueueMenu rankedQueueMenu;

    public MenuManager() {
        this.unrankedQueueMenu = new QueueMenu(false);
        this.rankedQueueMenu = new QueueMenu(true);
    }
}