package me.monkey_cat.bungeecordwhitelistct.config;

import me.monkey_cat.bungeecordwhitelistct.utils.config.FileConfig;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.nio.file.Path;
import java.util.*;

public class Config extends FileConfig {
    private final HashMap<String, HashSet<String>> serverWhitelist = new HashMap<>();

    public Config(Path configPath) {
        super(configPath, "config.yml");
    }

    @Override
    public void parse() {
        serverWhitelist.clear();
        Map<String, Set<String>> groups = getItem("groups", false);
        Map<String, Set<String>> whitelist = getItem("whitelist", false);
        Map<String, Set<String>> specialWhitelist = getItem("specialWhitelist", false);

        for (Map.Entry<String, Set<String>> groupEntry : groups.entrySet()) {
            String label = groupEntry.getKey();
            for (String serverName : groupEntry.getValue()) {
                serverWhitelist.computeIfAbsent(serverName, k -> new HashSet<>())
                        .addAll(whitelist.getOrDefault(label, new HashSet<>()));
            }
        }

        for (Map.Entry<String, Set<String>> specialEntry : specialWhitelist.entrySet()) {
            String username = specialEntry.getKey();
            for (String serverName : specialEntry.getValue()) {
                if (serverName.startsWith("!")) {
                    serverWhitelist.computeIfPresent(serverName.substring(1), (k, v) -> {
                        v.remove(username);
                        return v;
                    });
                } else {
                    HashSet<String> tmp = serverWhitelist.getOrDefault(serverName, new HashSet<>());
                    tmp.add(username);
                    serverWhitelist.put(serverName, tmp);
                }
            }
        }
    }

    private Map<String, List<String>> setToListType(Map<String, Set<String>> old) {
        Map<String, List<String>> data = new HashMap<>();
        old.forEach((s, o) -> data.put(s, o.stream().map(Object::toString).toList()));
        return data;
    }

    public boolean isEnable() {
        tryLoad();
        return configuration.getBoolean("enable");
    }

    public void setWhitelistEnable(Boolean enable) {
        configuration.set("enable", enable);
    }

    public boolean hasInWhitelist(String serverName, ProxiedPlayer player) {
        return hasInWhitelist(serverName, player.getName());
    }

    public boolean hasInWhitelist(String serverName, String playerName) {
        tryLoad();
        return serverWhitelist.containsKey(serverName) && serverWhitelist.get(serverName).contains(playerName);
    }

    public Map<String, Set<String>> getGroups() {
        return getItem("groups");
    }

    public void setGroups(Map<String, Set<String>> groups) {
        configuration.set("groups", setToListType(groups));
        save();
    }

    public Map<String, Set<String>> getWhitelist() {
        return getItem("whitelist");
    }

    public void setWhitelist(Map<String, Set<String>> whitelist) {
        configuration.set("whitelist", setToListType(whitelist));
        save();
    }

    public Map<String, Set<String>> getSpecialWhitelist() {
        return getItem("specialWhitelist");
    }

    public void setSpecialWhitelist(Map<String, Set<String>> specialWhitelist) {
        configuration.set("specialWhitelist", setToListType(specialWhitelist));
        save();
    }
}
