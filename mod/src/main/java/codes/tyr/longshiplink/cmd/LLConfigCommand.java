package codes.tyr.longshiplink.cmd;

import codes.tyr.longshiplink.LongshipLink;
import codes.tyr.longshiplink.LongshipLinkClient;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class LLConfigCommand {
    public static final List<String> COLORS = Arrays.asList(
        "black",
        "dark_blue",
        "dark_green",
        "dark_aqua",
        "dark_red",
        "dark_purple",
        "gold",
        "gray",
        "dark_gray",
        "blue",
        "green",
        "aqua",
        "red",
        "light_purple",
        "yellow",
        "white"
    );
    public static final Map<String, Character> COLOR_CODES = Map.ofEntries(
        Map.entry("black", '0'),
        Map.entry("dark_blue", '1'),
        Map.entry("dark_green", '2'),
        Map.entry("dark_aqua", '3'),
        Map.entry("dark_red", '4'),
        Map.entry("dark_purple", '5'),
        Map.entry("gold", '6'),
        Map.entry("gray", '7'),
        Map.entry("dark_gray", '8'),
        Map.entry("blue", '9'),
        Map.entry("green", 'a'),
        Map.entry("aqua", 'b'),
        Map.entry("red", 'c'),
        Map.entry("light_purple", 'd'),
        Map.entry("yellow", 'e'),
        Map.entry("white", 'f')
    );
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            addCommand(dispatcher);
        });
    }

    private static void addCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("llconfig")
            .then(ClientCommandManager.argument("key", StringArgumentType.word())
                .suggests(LLConfigCommand::getKeySuggestions)
                .executes(LLConfigCommand::handleKeyCommand)
                .then(ClientCommandManager.argument("value", StringArgumentType.word())
                    .suggests(LLConfigCommand::getValueSuggestions)
                    .executes(LLConfigCommand::handleKeyValueCommand)
                )
            )
        );
    }
    private static CompletableFuture<Suggestions> getKeySuggestions(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) {
        // Add your key suggestions here
        builder.suggest("color");

        return builder.buildFuture();
    }
    private static CompletableFuture<Suggestions> getValueSuggestions(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) {
        String configKey = StringArgumentType.getString(context, "key");

        return switch (configKey) {
            case "color" -> {
                for (String color : COLORS) {
                    builder.suggest(color);
                }
                yield builder.buildFuture();
            }
            default -> builder.buildFuture();
        };
    }
    private static int handleKeyCommand(CommandContext<FabricClientCommandSource> context) {
        String configKey = StringArgumentType.getString(context, "key");

        return switch (configKey) {
            case "color" -> {
                getColor();
                yield 1;
            }
            default -> 0;
        };
    }

    private static int handleKeyValueCommand(CommandContext<FabricClientCommandSource> context) {
        String configKey = StringArgumentType.getString(context, "key");
        String configValue = StringArgumentType.getString(context, "value");

        return switch (configKey) {
            case "color" -> {
                if (!COLOR_CODES.containsKey(configValue)) {
                    // TODO Send error to User
                    LongshipLink.LOGGER.error("Invalid color: " + configValue);
                    yield 0;
                }
                setColor(COLOR_CODES.get(configValue));
                yield 1;
            }
            default -> 0;
        };
    }

    private static void getColor() {
        Character color = LongshipLinkClient.pn.getUserColor();
        LongshipLink.LOGGER.info("Current color: " + color);
    }
    private static void setColor(Character color) {
        LongshipLinkClient.pn.setUserColor(color);
    }
}
