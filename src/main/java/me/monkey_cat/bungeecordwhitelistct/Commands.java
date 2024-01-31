package me.monkey_cat.bungeecordwhitelistct;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.monkey_cat.bungeecordwhitelistct.config.Config;
import me.monkey_cat.bungeecordwhitelistct.config.Message;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static me.monkey_cat.bungeecordwhitelistct.utils.Command.*;
import static me.monkey_cat.bungeecordwhitelistct.utils.Utils.getAllPlayersName;
import static me.monkey_cat.bungeecordwhitelistct.utils.Utils.getAllServersName;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;
import static net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.component;

public class Commands extends Command implements TabExecutor {
    static public final String GROUP_NAME = "GroupName";
    static public final String PLAYER_NAME = "PlayerName";
    static public final String SERVER_NAME = "ServerName";
    public final Config config;
    public final Message msgCnf;
    private final BungeeCordWhitelistCT plugin;
    private final CommandDispatcher<CommandSender> dispatcher;

    public Commands(BungeeCordWhitelistCT plugin) {
        super("whitelistct");

        LiteralArgumentBuilder<CommandSender> root = literal("")
                .requires(hasPermission("player"))
                .then(literal("groups")
                        .then(literal("list").executes(this::showGroups)
                                .then(argumentGroupName().executes(this::showGroups)
                                        .then(literal("players").executes(this::showGroupPlayers))))

                        .then(literal("create").requires(hasPermission("groups")).executes(missingArg(GROUP_NAME))
                                .then(argument(GROUP_NAME, word()).executes(this::createGroup)))

                        .then(literal("delete").requires(hasPermission("groups"))
                                .executes(missingArg(GROUP_NAME)).then(argumentGroupName().executes(this::deleteGroup)))

                        .then(literal("add").requires(hasPermission("player")).executes(missingArg(GROUP_NAME))
                                .then(argumentGroupName().executes(missingArg(PLAYER_NAME))
                                        .then(argument(PLAYER_NAME, word()).suggests(this::suggestGroupNoPlayers).executes(this::addGroupWhitelist)))
                        )

                        .then(literal("remove").requires(hasPermission("player")).executes(missingArg(GROUP_NAME))
                                .then(argumentGroupName().executes(missingArg(PLAYER_NAME))
                                        .then(argument(PLAYER_NAME, word()).suggests(this::suggestGroupPlayers).executes(this::removeGroupWhitelist)))
                        )
                )
                .then(literal("add").executes(missingArg(SERVER_NAME))
                        .then(argumentServerName().executes(missingArg(PLAYER_NAME)).then(argumentPlayerName().executes(this::addServerWhitelist))))
                .then(literal("remove").executes(missingArg(SERVER_NAME))
                        .then(argumentServerName().executes(missingArg(PLAYER_NAME)).then(argumentPlayerName().executes(this::removeServerWhitelist))))
                .then(literal("on").executes(this::setEnable))
                .then(literal("off").executes(this::setDisable))
                .then(literal("move").requires(hasPermission("move")).executes(missingArg(PLAYER_NAME))
                        .then(argumentPlayerName().executes(missingArg(GROUP_NAME)).then(argumentGroupName()).executes(this::movePlayerGroup)));

        this.plugin = plugin;
        msgCnf = plugin.message;
        config = plugin.config;
        dispatcher = new CommandDispatcher<>();
        dispatcher.register(root);
    }

    /**
     * - .admin
     * - .commands{name}
     */
    private Predicate<CommandSender> _hasPermission(String name) {
        return (sender) -> sender.hasPermission(BungeeCordWhitelistCTMeta.ID + ".admin")
                || sender.hasPermission(BungeeCordWhitelistCTMeta.ID + ".commands" + name);
    }

    /**
     * - .admin
     * - .commands
     */
    private Predicate<CommandSender> hasPermission() {
        return _hasPermission("");
    }

    /**
     * - .admin
     * - .commands.{name}
     */
    private Predicate<CommandSender> hasPermission(String name) {
        return _hasPermission("." + name);
    }

    /**
     * Has command send has permission
     */
    @Override
    public boolean hasPermission(CommandSender sender) {
        return hasPermission().test(sender);
    }

    private RequiredArgumentBuilder<CommandSender, String> argumentGroupName() {
        return argument(GROUP_NAME, word()).suggests(this::suggestGroups);
    }

    private CompletableFuture<Suggestions> suggestGroups(CommandContext<CommandSender> ctx, SuggestionsBuilder suggestions) {
        return suggestMatching(config.getGroups().keySet(), suggestions);
    }

    private CompletableFuture<Suggestions> suggestGroupNoPlayers(CommandContext<CommandSender> ctx, SuggestionsBuilder suggestions) {
        final String groupName = getString(ctx, GROUP_NAME);
        final Set<String> players = config.getWhitelist().getOrDefault(groupName, Collections.emptySet());

        return suggestMatching(getAllPlayersName(plugin).stream().filter(s -> !players.contains(s)).toList(), suggestions);
    }

    private CompletableFuture<Suggestions> suggestGroupPlayers(CommandContext<CommandSender> ctx, SuggestionsBuilder suggestions) {
        final String groupName = getString(ctx, GROUP_NAME);
        return suggestMatching(config.getWhitelist().getOrDefault(groupName, Collections.emptySet()), suggestions);
    }

    private RequiredArgumentBuilder<CommandSender, String> argumentPlayerName() {
        return argument(PLAYER_NAME, word()).suggests(this::suggestPlayers);
    }

    private CompletableFuture<Suggestions> suggestPlayers(CommandContext<CommandSender> ctx, SuggestionsBuilder suggestions) {
        return suggestMatching(getAllPlayersName(plugin), suggestions);
    }

    private RequiredArgumentBuilder<CommandSender, String> argumentServerName() {
        return argument(SERVER_NAME, word()).suggests(this::suggestServer);
    }

    private CompletableFuture<Suggestions> suggestServer(CommandContext<CommandSender> ctx, SuggestionsBuilder suggestions) {
        return suggestMatching(getAllServersName(plugin), suggestions);
    }

    private com.mojang.brigadier.Command<CommandSender> missingArg(String arg) {
        return ctx -> missingArg(arg, ctx);
    }

    private int missingArg(String arg, CommandContext<CommandSender> ctx) {
        final Audience audience = plugin.adventure().sender(ctx.getSource());

        audience.sendMessage(miniMessage().deserialize(msgCnf.getMissingArgument(),
                component("arg_name", Component.text(arg))));
        return 0;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        try {
            ParseResults<CommandSender> parse = dispatcher.parse(" " + String.join(" ", args), sender);
            try {
                dispatcher.execute(parse);
            } catch (CommandSyntaxException error) {
                sendHelp(sender);
            }
        } catch (Exception error) {
            sendHelp(sender);
        }
    }


    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        final CompletableFuture<Suggestions> future = dispatcher.getCompletionSuggestions(getParse(sender, args));
        if (!future.isCompletedExceptionally()) {
            return () -> future.join().getList().stream().map(Suggestion::getText).iterator();
        }
        return Collections::emptyIterator;
    }

    private ParseResults<CommandSender> getParse(CommandSender sender, String[] args) {
        return dispatcher.parse(" " + String.join(" ", args), sender);
    }

    private int addServerWhitelist(CommandContext<CommandSender> ctx) {
        final String serverName = getString(ctx, SERVER_NAME);
        final String playerName = getString(ctx, PLAYER_NAME);
        final Audience audience = plugin.adventure().sender(ctx.getSource());

        Map<String, Set<String>> specialWhitelist = config.getSpecialWhitelist();
        Set<String> servers = specialWhitelist.getOrDefault(playerName, new HashSet<>());
        servers.add(serverName);
        servers.remove("!" + serverName);
        specialWhitelist.put(playerName, servers);
        config.setSpecialWhitelist(specialWhitelist);

        audience.sendMessage(miniMessage().deserialize(msgCnf.getServerAddPlayerCompleted(),
                component("server_name", text(serverName)),
                component("player_name", text(playerName))
        ));
        return 0;
    }

    private void sendHelp(CommandSender sender) {
        final Audience audience = plugin.adventure().sender(sender);
        audience.sendMessage(miniMessage().deserialize(msgCnf.getHelp(),
                component("prefix", text(getName()))
        ));
    }

    private int removeServerWhitelist(CommandContext<CommandSender> ctx) {
        final String serverName = getString(ctx, SERVER_NAME);
        final String playerName = getString(ctx, PLAYER_NAME);
        final Audience audience = plugin.adventure().sender(ctx.getSource());

        Map<String, Set<String>> specialWhitelist = config.getSpecialWhitelist();
        Set<String> servers = specialWhitelist.getOrDefault(playerName, new HashSet<>());
        servers.remove(serverName);

        if (config.hasInWhitelist(serverName, playerName)) {
            servers.add("!" + serverName);
        }
        if (servers.isEmpty()) specialWhitelist.remove(playerName);
        else specialWhitelist.put(playerName, servers);
        config.setSpecialWhitelist(specialWhitelist);

        audience.sendMessage(miniMessage().deserialize(msgCnf.getServerRemovePlayerCompleted(),
                component("server_name", text(serverName)),
                component("player_name", text(playerName))
        ));
        return 0;
    }

    private int showGroups(CommandContext<CommandSender> ctx) {
        CommandSender source = ctx.getSource();
        final Audience audience = plugin.adventure().sender(source);
        Optional<String> groupNameOpt = getStringOpt(ctx, GROUP_NAME);
        if (groupNameOpt.isPresent()) {
            String groupName = groupNameOpt.get();
            Set<String> groups = config.getGroups().getOrDefault(groupName, null);
            if (groups == null) {
                audience.sendMessage(miniMessage().deserialize(msgCnf.getGroupNotFound()));
                return -1;
            }

            int serverCount = groups.size() - 1;
            List<String> serverNames = getAllServersName(plugin);
            List<String> serverList = groups.stream().sorted().toList();
            Component message = miniMessage().deserialize(msgCnf.getGroupShowServers(), component("group_name", text(groupName)));
            for (int i = 0; i <= serverCount; i++) {
                String serverName = serverList.get(i);
                Component tmp = text(" " + serverName, NamedTextColor.GRAY);
                if (!serverNames.contains(serverName)) {
                    tmp = tmp.color(NamedTextColor.RED).hoverEvent(HoverEvent.showText(miniMessage().deserialize(
                            msgCnf.getServerInvalid(),
                            component("server_name", text(serverName)))
                    ));
                }
                message = message.append(tmp);
                if (i < serverCount) {
                    message = message.append(text(",", NamedTextColor.GRAY));
                }
            }
            audience.sendMessage(message);
        } else {
            List<String> groupsList = config.getGroups().keySet().stream().sorted().toList();
            Component message = miniMessage().deserialize(msgCnf.getGroupShow());
            audience.sendMessage(message.append(text(" " + String.join(", ", groupsList), NamedTextColor.GRAY)));
        }
        return 0;
    }

    private int setEnable(CommandContext<CommandSender> ctx) {
        final Audience audience = plugin.adventure().sender(ctx.getSource());

        config.setWhitelistEnable(true);
        config.save();

        audience.sendMessage(miniMessage().deserialize(msgCnf.getEnable()));
        return 0;
    }

    private int setDisable(CommandContext<CommandSender> ctx) {
        final Audience audience = plugin.adventure().sender(ctx.getSource());

        config.setWhitelistEnable(false);
        config.save();

        audience.sendMessage(miniMessage().deserialize(msgCnf.getDisable()));
        return 0;
    }

    private int movePlayerGroup(CommandContext<CommandSender> ctx) {
        final Audience audience = plugin.adventure().sender(ctx.getSource());
        String groupName = getString(ctx, GROUP_NAME);
        String playerName = getString(ctx, PLAYER_NAME);
        Map<String, Set<String>> whitelist = config.getWhitelist();
        Set<String> players = whitelist.getOrDefault(groupName, null);

        if (players == null) {
            audience.sendMessage(miniMessage().deserialize(msgCnf.getGroupNotFound()));
            return -1;
        }

        for (var data : whitelist.entrySet()) {
            data.getValue().remove(playerName);
        }
        players.add(playerName);
        config.setWhitelist(whitelist);

        audience.sendMessage(miniMessage().deserialize(msgCnf.getPlayerMoveToGroup(),
                component("group_name", text(groupName))));
        return 0;
    }

    private int createGroup(CommandContext<CommandSender> ctx) {
        final Audience audience = plugin.adventure().sender(ctx.getSource());
        String groupName = getString(ctx, GROUP_NAME);
        Map<String, Set<String>> groups = config.getGroups();
        if (groups.containsKey(groupName)) {
            audience.sendMessage(miniMessage().deserialize(msgCnf.getGroupAlreadyExists(),
                    component("group_name", text(groupName))));
            return -1;
        }
        groups.put(groupName, new HashSet<>());
        config.setGroups(groups);
        audience.sendMessage(miniMessage().deserialize(msgCnf.getGroupCreateCompleted(),
                component("group_name", text(groupName))));
        return 0;
    }

    private int deleteGroup(CommandContext<CommandSender> ctx) {
        final Audience audience = plugin.adventure().sender(ctx.getSource());
        String groupName = getString(ctx, GROUP_NAME);
        Map<String, Set<String>> groups = config.getGroups();
        if (!groups.containsKey(groupName)) {
            audience.sendMessage(miniMessage().deserialize(msgCnf.getGroupNotFound(),
                    component("group_name", text(groupName))));
            return -1;
        }
        groups.remove(groupName);
        config.setGroups(groups);
        audience.sendMessage(miniMessage().deserialize(msgCnf.getGroupDeleteCompleted(),
                component("group_name", text(groupName))));
        return 0;
    }

    private int showGroupPlayers(CommandContext<CommandSender> ctx) {
        final Audience audience = plugin.adventure().sender(ctx.getSource());
        Optional<String> groupNameOpt = getStringOpt(ctx, GROUP_NAME);

        if (groupNameOpt.isPresent()) {
            String groupName = groupNameOpt.get();
            Set<String> players = config.getWhitelist().getOrDefault(groupName, null);

            if (players == null) {
                audience.sendMessage(miniMessage().deserialize(msgCnf.getGroupNotFound()));
                return -1;
            }

            Component message = miniMessage().deserialize(msgCnf.getGroupShowPlayers(),
                    component("group_name", text(groupName)));
            List<String> playersList = players.stream().sorted().toList();
            audience.sendMessage(message.append(Component.text(" " + String.join(", ", playersList), NamedTextColor.GRAY)));
            return 0;
        }

        audience.sendMessage(miniMessage().deserialize(msgCnf.getGroupNotFound()));
        return -1;
    }

    private int addGroupWhitelist(CommandContext<CommandSender> ctx) {
        final Audience audience = plugin.adventure().sender(ctx.getSource());
        String groupName = getString(ctx, GROUP_NAME);
        String playerName = getString(ctx, PLAYER_NAME);

        Map<String, Set<String>> whitelist = config.getWhitelist();
        Set<String> players = whitelist.getOrDefault(groupName, new HashSet<>());
        players.add(playerName);
        whitelist.put(groupName, players);
        config.setWhitelist(whitelist);

        audience.sendMessage(miniMessage().deserialize(msgCnf.getGroupAddPlayerCompleted(),
                component("group_name", text(groupName)),
                component("player_name", text(playerName)))
        );
        return 0;
    }

    private int removeGroupWhitelist(CommandContext<CommandSender> ctx) {
        final Audience audience = plugin.adventure().sender(ctx.getSource());
        String groupName = getString(ctx, GROUP_NAME);
        String playerName = getString(ctx, PLAYER_NAME);

        Map<String, Set<String>> whitelist = config.getWhitelist();
        Set<String> players = whitelist.getOrDefault(groupName, new HashSet<>());
        players.remove(playerName);
        whitelist.put(groupName, players);
        config.setWhitelist(whitelist);

        audience.sendMessage(miniMessage().deserialize(msgCnf.getGroupRemovePlayerCompleted(),
                component("group_name", text(groupName)),
                component("player_name", text(playerName)))
        );
        return 0;
    }
}
