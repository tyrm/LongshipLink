package codes.tyr.longshiplink.event;

import codes.tyr.longshiplink.LongshipLink;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;

public class LLChatMessageHandler {
    public static void register() {
        ServerMessageEvents.CHAT_MESSAGE.register((text, serverPlayerEntity, params) -> {
            LongshipLink.LOGGER.info("Chat message from " + serverPlayerEntity.getName().getString() + ": " + text.getSignedContent());
        });
    }
}
