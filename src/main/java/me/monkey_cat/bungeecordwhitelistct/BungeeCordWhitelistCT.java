package me.monkey_cat.bungeecordwhitelistct;

import me.monkey_cat.bungeecordwhitelistct.config.Config;
import net.md_5.bungee.api.plugin.Plugin;

public final class BungeeCordWhitelistCT extends Plugin {
    public final Config config;

    public BungeeCordWhitelistCT() {
        this.config = new Config(getDataFolder().toPath().resolve("config.yml"));
    }

    @Override
    public void onEnable() {
        getLogger().info(BungeeCordWhitelistCTMeta.NAME + "Enable!!");
        getProxy().getPluginManager().registerCommand(this, new Commands(this));

        // register events
        new Events(this).register();
    }
}
