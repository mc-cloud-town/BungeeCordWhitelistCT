package me.monkey_cat.bungeecordwhitelistct.utils;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.md_5.bungee.api.CommandSender;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;

public class Command {
    public static LiteralArgumentBuilder<CommandSender> literal(final String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    public static <T> RequiredArgumentBuilder<CommandSender, T> argument(final String name, final ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    public static Optional<String> getStringOpt(final CommandContext<?> ctx, final String name) {
        try {
            return Optional.ofNullable(getString(ctx, name));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }

    public static CompletableFuture<Suggestions> suggestMatching(List<String> suggestions, SuggestionsBuilder suggestionsBuilder) {
        return suggestMatching(suggestions::iterator, suggestionsBuilder);
    }

    public static CompletableFuture<Suggestions> suggestMatching(Iterable<String> suggestions, SuggestionsBuilder suggestionsBuilder) {
        String remaining = suggestionsBuilder.getRemaining().toLowerCase();

        for (String suggestion : suggestions) {
            if (suggestion.toLowerCase().startsWith(remaining)) {
                suggestionsBuilder.suggest(suggestion);
            }
        }

        return suggestionsBuilder.buildFuture();
    }
}
