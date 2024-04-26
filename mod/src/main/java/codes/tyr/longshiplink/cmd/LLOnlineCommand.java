package codes.tyr.longshiplink.cmd;

import codes.tyr.longshiplink.LongshipLinkClient;
import codes.tyr.longshiplink.pubnub.ClientConnection;
import com.mojang.brigadier.CommandDispatcher;
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
                LongshipLinkClient.pn.updateOnlineUsers();
                List<String> onlineUsers = LongshipLinkClient.pn.getOnlineUsers();
                MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.of(ClientConnection.GLOBAL_PREFIX + "§7§oPlayers Online:§r " + String.join(", ", onlineUsers)));

                return 1;  // Indicates command executed successfully
            }
        ));
    }
}
