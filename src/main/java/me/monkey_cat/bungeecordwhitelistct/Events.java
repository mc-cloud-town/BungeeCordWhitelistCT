package me.monkey_cat.bungeecordwhitelistct;

import net.kyori.adventure.audience.Audience;
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

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;
import static net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.component;

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
    public void onServerConnectEvent(ServerConnectEvent event) {
        if (!plugin.config.isEnable()) return;

        ProxiedPlayer player = event.getPlayer();
        String newServerName = event.getTarget().getName();
        if (plugin.config.hasInWhitelist(newServerName, player)) {
            return;
        }
        final Audience audience = plugin.adventure().sender(player);


        @Nullable Server oldServer = player.getServer();
        if (oldServer == null) {
            for (Map.Entry<String, ServerInfo> server : plugin.getProxy().getServers().entrySet()) {
                if (plugin.config.hasInWhitelist(server.getKey(), player)) {
                    event.setTarget(server.getValue());

                    audience.sendMessage(miniMessage().deserialize(plugin.message.getAutoGuidanceMoveMessage(),
                            component("old_server", text(newServerName)),
                            component("new_server", text(server.getKey()))
                    ));
                    return;
                }
            }
            player.disconnect(colorize(plugin.message.getKickMessage()));
        } else {
            event.setCancelled(true);
            audience.sendMessage(miniMessage().deserialize(plugin.message.getTryMoveKickMessage(),
                    component("old_server", text(oldServer.getInfo().getName())),
                    component("new_server", text(newServerName))
            ));
        }
    }

    public TextComponent colorize(String message) {
        return new TextComponent(ChatColor.translateAlternateColorCodes('&', message));
    }
}
