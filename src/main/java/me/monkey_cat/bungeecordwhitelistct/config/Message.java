package me.monkey_cat.bungeecordwhitelistct.config;

import me.monkey_cat.bungeecordwhitelistct.utils.config.FileConfig;

import java.nio.file.Path;

public class Message extends FileConfig {
    public Message(Path configPath) {
        super(configPath, "message.yml");
    }

    protected String tryGetString(String path) {
        this.tryLoad();
        return this.configuration.getString(path);
    }

    public String getEnable() {
        return this.tryGetString("enable");
    }

    public String getDisable() {
        return this.tryGetString("disable");
    }

    public String getKickMessage() {
        return this.tryGetString("kickMessage");
    }

    public String getTryMoveKickMessage() {
        return this.tryGetString("tryMoveKickMessage");
    }

    public String getAutoGuidanceMoveMessage() {
        return this.tryGetString("autoGuidanceMoveMessage");
    }

    public String getServerInvalid() {
        return this.tryGetString("serverInvalid");
    }

    public String getGroupNotFound() {
        return this.tryGetString("groupNotFound");
    }

    public String getGroupShow() {
        return this.tryGetString("groupShow");
    }

    public String getMissingArgument() {
        return this.tryGetString("missingArgument");
    }

    public String getGroupShowPlayers() {
        return this.tryGetString("groupShowPlayers");
    }

    public String getGroupShowServers() {
        return this.tryGetString("groupShowServers");
    }

    public String getGroupCreateCompleted() {
        return this.tryGetString("groupCreateCompleted");
    }

    public String getGroupAlreadyExists() {
        return this.tryGetString("groupAlreadyExists");
    }

    public String getGroupDeleteCompleted() {
        return this.tryGetString("groupDeleteCompleted");
    }

    public String getGroupAddPlayerCompleted() {
        return this.tryGetString("groupAddPlayerCompleted");
    }

    public String getGroupRemovePlayerCompleted() {
        return this.tryGetString("groupRemovePlayerCompleted");
    }

    public String getServerAddPlayerCompleted() {
        return this.tryGetString("serverAddPlayerCompleted");
    }

    public String getServerRemovePlayerCompleted() {
        return this.tryGetString("serverRemovePlayerCompleted");
    }
}
