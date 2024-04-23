package codes.tyr.longshiplink.event;

import codes.tyr.longshiplink.LongshipLink;
import codes.tyr.longshiplink.LongshipLinkClient;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;

public class LLChatMessageHandler {
    public static void register() {
        ClientSendMessageEvents.CHAT.register((text) -> {
            LongshipLink.LOGGER.info("Chat message: " + text);
            //LongshipLinkClient.pn.sendChatMessage(MinecraftClient.getInstance().getSession().getUsername(), text);
        });
    }
}
