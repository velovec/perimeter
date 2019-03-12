package ru.v0rt3x.themis.server.flag;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class FlagCollector {

    private final Set<String> flags = new HashSet<>();

    public boolean hasFlag(String flag) {
        return flags.contains(flag);
    }

    public void saveFlag(String flag) {
        flags.add(flag);
    }

    public void removeFlag(String flag) {
        flags.remove(flag);
    }
}
