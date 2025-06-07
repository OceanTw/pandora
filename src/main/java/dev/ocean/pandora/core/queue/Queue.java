package dev.ocean.pandora.core.queue;

import dev.ocean.pandora.core.kit.Kit;
import dev.ocean.pandora.core.player.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class Queue {
    private User user;
    private Kit kit;
    private boolean ranked;
    private long joinTime;

    public long getQueueTime() {
        return System.currentTimeMillis() - joinTime;
    }

    public int getQueueTimeSeconds() {
        return (int) (getQueueTime() / 1000);
    }
}