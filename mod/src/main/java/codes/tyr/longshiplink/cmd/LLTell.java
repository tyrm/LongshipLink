package codes.tyr.longshiplink.cmd;

import codes.tyr.longshiplink.LongshipLinkClient;
import codes.tyr.longshiplink.pubnub.ClientConnection;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import java.util.concurrent.CompletableFuture;

public class LLTell {
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            addCommand(dispatcher);
        });
    }
    private static void addCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("lltell")
            .then(ClientCommandManager.argument("username", StringArgumentType.word())
                .suggests(LLTell::getUserSuggestions)
                .then(ClientCommandManager.argument("message", StringArgumentType.greedyString())
                    .executes(context -> {
                        String username = StringArgumentType.getString(context, "username");
                        String message = StringArgumentType.getString(context, "message");

                        String RecipientUID = LongshipLinkClient.pn.findUID(username);
                        if (RecipientUID == null) {
                            LongshipLinkClient.writeHUD("User " + username + " not found.");
                            return 0;
                        }

                        LongshipLinkClient.pn.sendTellMessage(RecipientUID, message);
                        LongshipLinkClient.writeHUD(ClientConnection.GLOBAL_PREFIX + "§7§oYou whisper to " + username + ": " + message +"§r");
                        return 1;  // Indicates command executed successfully
                    })
                )
            )
        );
    }
    private static CompletableFuture<Suggestions> getUserSuggestions(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) {
        LongshipLinkClient.pn.
                getOnlineUsers(LongshipLinkClient.pn.getUsername()).
                forEach(builder::suggest);

        return builder.buildFuture();
    }
}
