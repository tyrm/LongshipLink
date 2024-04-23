package codes.tyr.longshiplink.event;

import codes.tyr.longshiplink.LongshipLink;
import codes.tyr.longshiplink.LongshipLinkClient;
import codes.tyr.longshiplink.config.LLClientConfigs;
import codes.tyr.longshiplink.network.LLMessages;
import codes.tyr.longshiplink.network.packet.LLAuthRequestPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;

public class LLClientConnectionHandler {
    public static void register() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            LongshipLink.LOGGER.info("Client " + client.getSession().getUsername() + " has joined the server");
            LongshipLinkClient.pn.startAuthLoop();
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            LongshipLink.LOGGER.info("Client " + client.getSession().getUsername() + " has disconnected from the server");

            LongshipLinkClient.pn.stopAuthLoop();
            LongshipLinkClient.pn.close();
        });
    }
}
