package codes.tyr.longshiplink.cmd;

import codes.tyr.longshiplink.LongshipLink;
import codes.tyr.longshiplink.LongshipLinkClient;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.List;

public class LLOnlineCommand {
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            addCommand(dispatcher);
        });
    }

    private static void addCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("llonline")
            .executes(context -> {
                LongshipLink.LOGGER.info("Executing llonline command");

                List<String> onlineUsers = LongshipLinkClient.pn.getOnlineUsers();
                LongshipLink.LOGGER.info("Online users: " + onlineUsers);

                MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.of("§7§oPlayers Online:§r " + String.join(", ", onlineUsers)));

                return 1;  // Indicates command executed successfully
            }
        ));
    }
}
