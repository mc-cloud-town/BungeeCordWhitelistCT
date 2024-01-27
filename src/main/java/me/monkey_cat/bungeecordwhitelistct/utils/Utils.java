package me.monkey_cat.bungeecordwhitelistct.utils;

import me.monkey_cat.bungeecordwhitelistct.BungeeCordWhitelistCT;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;

public class Utils {
    public static List<String> getAllServersName(BungeeCordWhitelistCT plugin) {
        return plugin.getProxy().getServers().keySet().stream().toList();
    }

    public static List<String> getAllPlayersName(BungeeCordWhitelistCT plugin) {
        return plugin.getProxy().getPlayers().stream().map(ProxiedPlayer::getName).toList();
    }
}
