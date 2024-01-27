package me.monkey_cat.bungeecordwhitelistct;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.monkey_cat.bungeecordwhitelistct.config.Config;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static me.monkey_cat.bungeecordwhitelistct.utils.Command.*;
import static me.monkey_cat.bungeecordwhitelistct.utils.Utils.getAllPlayersName;
import static me.monkey_cat.bungeecordwhitelistct.utils.Utils.getAllServersName;

public class Commands extends Command implements TabExecutor {
    static public final String GROUP_NAME = "GroupName";
    static public final String PLAYER_NAME = "PlayerName";
    static public final String SERVER_NAME = "ServerName";
    public final Config config;
    private final BungeeCordWhitelistCT plugin;
    private final CommandDispatcher<CommandSender> dispatcher;

    public Commands(BungeeCordWhitelistCT plugin) {
        super("whitelistct");

        var root = literal("")
                .then(
                        literal("groups")
                                .then(literal("list").executes(this::showGroups)
                                        .then(argumentGroupName().executes(this::showGroups)
//                                                .then(literal("players").executes(this::showGroupPlayers)))
                                        ))

                                .then(literal("create").executes(missingArg(GROUP_NAME))
//                                        .then(argument(GROUP_NAME, word()).executes(this::createGroup))
                                )

                                .then(literal("delete").executes(missingArg(GROUP_NAME))
//                                        .then(argumentGroupName().executes(this::deleteGroup))
                                )

                                .then(literal("add").executes(missingArg(GROUP_NAME))
//                                        .then(argumentGroupName().executes(missingArg(PLAYER_NAME)).then(argumentPlayerName().executes(this::addGroupWhitelist)))
                                )

                                .then(literal("remove").executes(missingArg(GROUP_NAME))
//                                        .then(argumentGroupName().executes(missingArg(PLAYER_NAME)).then(argumentPlayerName().executes(this::removeGroupWhitelist)))
                                )
                )
                .then(literal("add").executes(missingArg(SERVER_NAME))
                        .then(argumentServerName().executes(missingArg(PLAYER_NAME)).then(argumentPlayerName().executes(this::addServerWhitelist))))
                .then(literal("remove").executes(missingArg(SERVER_NAME))
                        .then(argumentServerName().executes(missingArg(PLAYER_NAME)).then(argumentPlayerName().executes(this::removeServerWhitelist))))
                .then(literal("on").executes(this::setEnable))
                .then(literal("off").executes(this::setDisable));

        this.plugin = plugin;
        config = plugin.config;
        dispatcher = new CommandDispatcher<>();
        dispatcher.register(root);
    }

    private RequiredArgumentBuilder<CommandSender, String> argumentGroupName() {
        return argument(GROUP_NAME, word()).suggests(this::suggestGroups);
    }

    private CompletableFuture<Suggestions> suggestGroups(CommandContext<CommandSender> ctx, SuggestionsBuilder suggestions) {
        return suggestMatching(plugin.config.getGroups().keySet(), suggestions);
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

    private int test(CommandContext<CommandSender> ctx) {
        plugin.getLogger().info("test");
        return 0;
    }

    private com.mojang.brigadier.Command<CommandSender> missingArg(String arg) {
        return ctx -> missingArg(arg, ctx);
    }

    private int missingArg(String arg, CommandContext<CommandSender> ctx) {
        ctx.getSource().sendMessage(new ComponentBuilder("test").create());
        return 0;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        try {
            ParseResults<CommandSender> parse = dispatcher.parse(" " + String.join(" ", args), sender);
            try {
                dispatcher.execute(parse);
            } catch (CommandSyntaxException e) {
                // TODO add help/error reply
                sender.sendMessage();
            }
        } catch (Exception ignored) {
            // TODO add help reply
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
        String serverName = getString(ctx, SERVER_NAME);
        String playerName = getString(ctx, PLAYER_NAME);

        Map<String, Set<String>> specialWhitelist = plugin.config.getSpecialWhitelist();
        Set<String> servers = specialWhitelist.getOrDefault(playerName, new HashSet<>());
        servers.add(serverName);
        specialWhitelist.put(playerName, servers);
        plugin.config.setSpecialWhitelist(specialWhitelist);

        TranslatableComponent msg = new TranslatableComponent("velocityct.whitelist.serverAddPlayerCompleted");
        msg.addWith(serverName);
        msg.addWith(playerName);
        ctx.getSource().sendMessage(msg);
        return 0;
    }

    private int removeServerWhitelist(CommandContext<CommandSender> ctx) {
        String serverName = getString(ctx, SERVER_NAME);
        String playerName = getString(ctx, PLAYER_NAME);

        Map<String, Set<String>> specialWhitelist = plugin.config.getSpecialWhitelist();
        Set<String> servers = specialWhitelist.getOrDefault(playerName, new HashSet<>());
        servers.remove(serverName);

        if (plugin.config.hasInWhitelist(serverName, playerName)) {
            servers.add("!" + serverName);
        }
        if (servers.isEmpty()) specialWhitelist.remove(playerName);
        else specialWhitelist.put(playerName, servers);
        plugin.config.setSpecialWhitelist(specialWhitelist);

        TranslatableComponent msg = new TranslatableComponent("velocityct.whitelist.serverRemovePlayerCompleted");
        msg.addWith(serverName);
        msg.addWith(playerName);
        ctx.getSource().sendMessage(msg);
        return 0;
    }

    private int showGroups(CommandContext<CommandSender> ctx) {
        CommandSender source = ctx.getSource();
        Optional<String> groupNameOpt = getStringOpt(ctx, GROUP_NAME);
        if (groupNameOpt.isPresent()) {
            String groupName = groupNameOpt.get();
            Set<String> groups = config.getGroups().getOrDefault(groupName, null);
            if (groups == null) {
                source.sendMessage(new TextComponent("velocityct.whitelist.groupNotFound"));
                return -1;
            }

            int serverCount = groups.size() - 1;
            List<String> serverNames = getAllServersName(plugin);
            List<String> serverList = groups.stream().sorted().toList();
            TextComponent message = new TextComponent("velocityct.whitelist.groupShowServers");
            message.addExtra(groupName);
            ComponentBuilder messages = new ComponentBuilder(message);
            for (int i = 0; i <= serverCount; i++) {
                String serverName = serverList.get(i);
                TextComponent tmp = new TextComponent(" " + serverName);
                tmp.setColor(ChatColor.GRAY);
                if (!serverNames.contains(serverName)) {
                    tmp.setColor(ChatColor.RED);

                    TextComponent haveText = new TextComponent("velocityct.serverInvalid");
                    haveText.addExtra(serverName);
                    tmp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new TextComponent[]{haveText})));
                }
                messages.append(tmp);
                if (i < serverCount) {
                    TextComponent splitText = new TextComponent(",");
                    splitText.setColor(ChatColor.GRAY);
                    messages.append(splitText);
                }
            }
            source.sendMessage(message);
        } else {
            TextComponent msg1 = new TextComponent("velocityct.whitelist.groupShow");
            msg1.setColor(ChatColor.DARK_AQUA);
            TextComponent msg2 = new TextComponent(String.join(", ", config.getGroups().keySet().stream().sorted().toList()));
            msg2.setColor(ChatColor.GRAY);
            ctx.getSource().sendMessage(msg1, msg2);
        }
        return 0;
    }

    private int setEnable(CommandContext<CommandSender> ctx) {
        config.setWhitelistEnable(true);
        ctx.getSource().sendMessage(new TranslatableComponent("velocityct.whitelist.enable"));
        return 0;
    }

    private int setDisable(CommandContext<CommandSender> ctx) {
        config.setWhitelistEnable(true);
        ctx.getSource().sendMessage(new TranslatableComponent("velocityct.whitelist.disable"));
        return 0;
    }
}
