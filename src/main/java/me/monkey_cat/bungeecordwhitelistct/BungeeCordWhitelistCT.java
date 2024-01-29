package me.monkey_cat.bungeecordwhitelistct;

import me.monkey_cat.bungeecordwhitelistct.config.Config;
import me.monkey_cat.bungeecordwhitelistct.config.Message;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.md_5.bungee.api.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.nio.file.Path;

public final class BungeeCordWhitelistCT extends Plugin {
    public final Config config;
    public final Message message;
    private BungeeAudiences adventure;

    public BungeeCordWhitelistCT() {
        Path dataFolderPath = getDataFolder().toPath();

        config = new Config(dataFolderPath.resolve("config.yml"));
        message = new Message(dataFolderPath.resolve("message.yml"));
    }

    public @NonNull BungeeAudiences adventure() {
        if (adventure == null) {
            throw new IllegalStateException("Cannot retrieve audience provider while plugin is not enabled");
        }
        return adventure;
    }

    @Override
    public void onEnable() {
        config.tryLoad();
        message.tryLoad();

        adventure = BungeeAudiences.create(this);

        getLogger().info(BungeeCordWhitelistCTMeta.NAME + "Enable!!");
        getProxy().getPluginManager().registerCommand(this, new Commands(this));

        // register events
        new Events(this).register();
    }

    @Override
    public void onDisable() {
        if (adventure != null) {
            adventure.close();
            adventure = null;
        }
    }
}
