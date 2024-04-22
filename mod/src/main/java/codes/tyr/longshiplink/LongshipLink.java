package codes.tyr.longshiplink;

import codes.tyr.longshiplink.config.LLServerConfigs;
import codes.tyr.longshiplink.event.LLChatMessageHandler;
import codes.tyr.longshiplink.network.LLMessages;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LongshipLink implements ModInitializer {
    public static final String MOD_ID = "longship-link";
    public static final Logger LOGGER = LogManager.getLogger("LongshipLink");
    @Override
    public void onInitialize() {
        LOGGER.info("Initializing LongshipLink");

        LLServerConfigs.registerConfigs(); // must be called first

        LLMessages.registerC2SPackets();

        LLChatMessageHandler.register();
    }
}
