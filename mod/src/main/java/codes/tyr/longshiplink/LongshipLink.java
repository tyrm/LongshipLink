package codes.tyr.longshiplink;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LongshipLink implements ModInitializer {
    public static final String MOD_ID = "longship-link";
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Runs the mod initializer.
     */
    @Override
    public void onInitialize() {;
        LOGGER.info("Initializing LongshipLink");
        ServerMessageEvents.CHAT_MESSAGE.register((text, serverPlayerEntity, params) -> {
            LOGGER.info("Chat message from " + serverPlayerEntity.getName().getString() + ": " + text.getSignedContent());
        });
    }
}
