package codes.tyr.longshiplink.event;

import codes.tyr.longshiplink.LongshipLink;
import codes.tyr.longshiplink.LongshipLinkClient;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class LLClientConnectionHandler {
    public static void register() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            LongshipLink.LOGGER.debug("Client " + client.getSession().getUsername() + " has joined the server");
            LongshipLinkClient.pn.startAuthLoop();
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            LongshipLink.LOGGER.debug("Client " + client.getSession().getUsername() + " has disconnected from the server");

            LongshipLinkClient.pn.stopAuthLoop();
            LongshipLinkClient.pn.close();
        });
    }
}
