package codes.tyr.longshiplink.cmd;

import codes.tyr.longshiplink.LongshipLink;
import codes.tyr.longshiplink.LongshipLinkClient;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class LLLLCommand {
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            addCommand(dispatcher);
        });
    }

    private static void addCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("ll")
            .then(ClientCommandManager.argument("text", StringArgumentType.greedyString())
            .executes(context -> {
                LongshipLink.LOGGER.info("Executing ll command");

                String input = StringArgumentType.getString(context, "text");
                LongshipLink.LOGGER.info("Input: " + input);

                LongshipLinkClient.pn.sendChatMessage(input);

                return 1;  // Indicates command executed successfully
            }
        )));
    }
}
