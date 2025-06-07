package dev.ocean.pandora.manager;

import dev.ocean.pandora.kit.Kit;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class KitManager {
    private final Map<String, Kit> kits = new HashMap<>();

    public void addKit(Kit kit) {
        kits.put(kit.getName(), kit);
    }

    public Kit getKit(String name) {
        return kits.get(name);
    }

    public List<Kit> getAvailableKits() {
        return kits.values().stream().collect(Collectors.toList());
    }

    public List<Kit> getBotKits() {
        return kits.values().stream()
                .filter(kit -> kit.getEnabledRules().contains(Kit.Rules.BOT))
                .collect(Collectors.toList());
    }

    public void removeKit(String name) {
        kits.remove(name);
    }
}