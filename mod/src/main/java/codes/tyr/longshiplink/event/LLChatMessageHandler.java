package codes.tyr.longshiplink.event;

import codes.tyr.longshiplink.LongshipLink;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;

public class LLChatMessageHandler {
    public static void register() {
        ClientSendMessageEvents.CHAT.register((text) -> {
            LongshipLink.LOGGER.info("Chat message: " + text);
            // TODO for auto forward
        });
    }
}
