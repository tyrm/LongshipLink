package codes.tyr.longshiplink;

import codes.tyr.longshiplink.cmd.LLLLCommand;
import codes.tyr.longshiplink.config.LLClientConfigs;
import codes.tyr.longshiplink.event.LLChatMessageHandler;
import codes.tyr.longshiplink.event.LLClientConnectionHandler;
import codes.tyr.longshiplink.network.LLMessages;
import codes.tyr.longshiplink.pubnub.ClientConnection;
import net.fabricmc.api.ClientModInitializer;

public class LongshipLinkClient implements ClientModInitializer {
    public static final ClientConnection pn = new ClientConnection();
    public static String serverID;

    @Override
    public void onInitializeClient() {
        LongshipLink.LOGGER.info("Initializing LongshipLink client");

        LLClientConfigs.registerConfigs(); // must be called first

        LLMessages.registerS2CPackets();

        LLClientConnectionHandler.register();

        LLLLCommand.register();
    }
}
