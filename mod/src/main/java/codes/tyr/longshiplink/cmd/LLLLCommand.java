package codes.tyr.longshiplink.cmd;

import codes.tyr.longshiplink.LongshipLink;
import com.mojang.brigadier.CommandDispatcher;
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
        dispatcher.register(ClientCommandManager.literal("example")
            .executes(context -> {
                LongshipLink.LOGGER.info("Executing example command");
                return 1;  // Indicates command executed successfully
            }
        ));
    }
}
