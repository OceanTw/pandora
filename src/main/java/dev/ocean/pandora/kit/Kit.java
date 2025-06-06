package dev.ocean.pandora.kit;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Data
public class Kit {
    private String name;
    private String displayName;
    private Material icon;

    private final List<Rules> enabledRules = new ArrayList<>();

    public enum Rules {
        BOXING, BUILD, BOT
    }
}
