package codes.tyr.longshiplink.event;

import codes.tyr.longshiplink.LongshipLink;
import codes.tyr.longshiplink.LongshipLinkClient;
import codes.tyr.longshiplink.network.LLMessages;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;

public class LLClientConnectionHandler {
    public static void register() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            LongshipLink.LOGGER.info("Client " + client.getSession().getUsername() + " has joined the server");

            try {
                ClientPlayNetworking.send(LLMessages.AUTH_REQUEST_ID, PacketByteBufs.create());
            } catch (Exception e) {
                LongshipLink.LOGGER.error("Error sending auth request: " + e.getMessage());
            }
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            LongshipLink.LOGGER.info("Client " + client.getSession().getUsername() + " has disconnected from the server");

            LongshipLinkClient.pn.close();
        });
    }
}
