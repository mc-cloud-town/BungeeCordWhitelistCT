package me.monkey_cat.bungeecordwhitelistct;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.logging.Logger;

public class Events implements Listener {
    protected final BungeeCordWhitelistCT plugin;
    protected final Logger logger;

    public Events(BungeeCordWhitelistCT plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public void register() {
        plugin.getProxy().getPluginManager().registerListener(plugin, this);
    }

    @EventHandler
    public void onServerConnectEvent(ServerConnectEvent event, String[] args) {
        if (!plugin.config.isWhitelistEnable()) return;

        ProxiedPlayer player = event.getPlayer();
        String newServerName = event.getTarget().getName();
        if (plugin.config.hasInWhitelist(newServerName, player)) {
            return;
        }

        @Nullable Server oldServer = player.getServer();
        if (oldServer == null) {
            for (Map.Entry<String, ServerInfo> server : plugin.getProxy().getServers().entrySet()) {
                if (plugin.config.hasInWhitelist(server.getKey(), player)) {
                    event.setTarget(server.getValue());
                    player.sendMessage(colorize(plugin.config.getAutoGuidanceMoveMessage()
                            .replace("<old_server>", newServerName)
                            .replace("<new_server>", server.getKey())));
                    return;
                }
            }
            player.disconnect(colorize(plugin.config.getKickMessage()));
        } else {
            event.setCancelled(true);
            player.sendMessage(colorize(plugin.config.getTryMoveKickMessage()
                    .replace("<old_server>", oldServer.getInfo().getName())
                    .replace("<new_server>", newServerName)));
        }
    }

    public TextComponent colorize(String message) {
        return new TextComponent(ChatColor.translateAlternateColorCodes('&', message));
    }
}
