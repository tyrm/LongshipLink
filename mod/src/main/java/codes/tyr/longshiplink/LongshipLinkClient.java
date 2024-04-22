package codes.tyr.longshiplink;

import codes.tyr.longshiplink.cmd.LLLLCommand;
import codes.tyr.longshiplink.event.LLClientConnectionHandler;
import codes.tyr.longshiplink.network.LLMessages;
import net.fabricmc.api.ClientModInitializer;

public class LongshipLinkClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        LongshipLink.LOGGER.info("Initializing LongshipLink client");

        LLMessages.registerS2CPackets();

        LLClientConnectionHandler.register();

        LLLLCommand.register();
    }
}
