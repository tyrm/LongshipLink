package codes.tyr.longshiplink;

import codes.tyr.longshiplink.cmd.LLConfigCommand;
import codes.tyr.longshiplink.cmd.LLLLCommand;
import codes.tyr.longshiplink.cmd.LLOnlineCommand;
import codes.tyr.longshiplink.cmd.LLTell;
import codes.tyr.longshiplink.event.LLClientConnectionHandler;
import codes.tyr.longshiplink.network.LLMessages;
import codes.tyr.longshiplink.pubnub.ClientConnection;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.UUID;

public class LongshipLinkClient implements ClientModInitializer {
    public static final ClientConnection pn = new ClientConnection();
    public static String serverID;

    @Override
    public void onInitializeClient() {
        LongshipLink.LOGGER.info("Initializing LongshipLink client");

        LLMessages.registerS2CPackets();

        LLClientConnectionHandler.register();

        LLLLCommand.register();
        LLConfigCommand.register();
        LLOnlineCommand.register();
        LLTell.register();
    }

    public static String MID() {
        String fauxMid = System.getenv("FAUX_MID");
        if (fauxMid != null && !fauxMid.isEmpty()) {
            LongshipLink.LOGGER.warn("Using faux MID: " + fauxMid);
            return fauxMid;
        }

        UUID mid = MinecraftClient.getInstance().getSession().getUuidOrNull();
        if (mid == null) {
            throw new RuntimeException("Cannot get Minecraft ID!");
        }

        return mid.toString();
    }

    public static void writeHUD(String message) {
        MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.of(message));
    }
}
