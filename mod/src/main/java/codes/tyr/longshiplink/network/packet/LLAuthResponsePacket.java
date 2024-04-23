package codes.tyr.longshiplink.network.packet;

import codes.tyr.longshiplink.LongshipLink;
import codes.tyr.longshiplink.LongshipLinkClient;
import codes.tyr.longshiplink.config.LLServerConfigs;
import codes.tyr.longshiplink.network.LLMessages;
import com.pubnub.api.PubNubException;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class LLAuthResponsePacket {
    public static void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        LongshipLink.LOGGER.info("Received authentication response packet from server");

        String uuid = buf.readString();
        String subKey = buf.readString();
        String pubKey = buf.readString();
        String serverID = buf.readString();
        String token = buf.readString();

        LongshipLinkClient.serverID = serverID;
        LongshipLinkClient.pn.setKeys(uuid, subKey, pubKey, client.getSession().getUsername());
    }

    public static void send(ServerPlayerEntity player, String subKey, String pubKey, String token) {
        LongshipLink.LOGGER.info("Sending authentication response packet to server");

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(player.getUuidAsString());
        buf.writeString(subKey);
        buf.writeString(pubKey);
        buf.writeString(LLServerConfigs.SERVER_ID);
        buf.writeString(token);

        try {
            ServerPlayNetworking.send(player, LLMessages.AUTH_RESPONSE_ID, buf);
        } catch (Exception e) {
            LongshipLink.LOGGER.error("Error sending auth request: " + e.getMessage());
        }
    }
}
