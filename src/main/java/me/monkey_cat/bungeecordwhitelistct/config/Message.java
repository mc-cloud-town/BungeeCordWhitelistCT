package me.monkey_cat.bungeecordwhitelistct.config;

import me.monkey_cat.bungeecordwhitelistct.utils.config.FileConfig;

import java.nio.file.Path;

public class Message extends FileConfig {
    public Message(Path configPath) {
        super(configPath, "message.yml");
    }

    protected String tryGetString(String path) {
        tryLoad();
        return configuration.getString(path);
    }

    public String getEnable() {
        return tryGetString("enable");
    }

    public String getDisable() {
        return tryGetString("disable");
    }

    public String getKickMessage() {
        return tryGetString("kickMessage");
    }

    public String getTryMoveKickMessage() {
        return tryGetString("tryMoveKickMessage");
    }

    public String getAutoGuidanceMoveMessage() {
        return tryGetString("autoGuidanceMoveMessage");
    }

    public String getServerInvalid() {
        return tryGetString("serverInvalid");
    }

    public String getGroupNotFound() {
        return tryGetString("groupNotFound");
    }

    public String getGroupShow() {
        return tryGetString("groupShow");
    }

    public String getMissingArgument() {
        return tryGetString("missingArgument");
    }

    public String getGroupShowPlayers() {
        return tryGetString("groupShowPlayers");
    }

    public String getGroupShowServers() {
        return tryGetString("groupShowServers");
    }

    public String getGroupCreateCompleted() {
        return tryGetString("groupCreateCompleted");
    }

    public String getGroupAlreadyExists() {
        return tryGetString("groupAlreadyExists");
    }

    public String getGroupDeleteCompleted() {
        return tryGetString("groupDeleteCompleted");
    }

    public String getGroupAddPlayerCompleted() {
        return tryGetString("groupAddPlayerCompleted");
    }

    public String getGroupRemovePlayerCompleted() {
        return tryGetString("groupRemovePlayerCompleted");
    }

    public String getServerAddPlayerCompleted() {
        return tryGetString("serverAddPlayerCompleted");
    }

    public String getServerRemovePlayerCompleted() {
        return tryGetString("serverRemovePlayerCompleted");
    }
}
