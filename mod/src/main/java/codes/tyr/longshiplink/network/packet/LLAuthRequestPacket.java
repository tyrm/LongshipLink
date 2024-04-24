package codes.tyr.longshiplink.network.packet;

import codes.tyr.longshiplink.LongshipLink;
import codes.tyr.longshiplink.auth.AuthClient;
import codes.tyr.longshiplink.auth.UserAuthResponse;
import codes.tyr.longshiplink.network.LLMessages;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public class LLAuthRequestPacket {
    public static void receive(MinecraftServer server, @NotNull ServerPlayerEntity player, ServerPlayNetworkHandler handler, @NotNull PacketByteBuf buf, PacketSender responseSender) {
        LongshipLink.LOGGER.info("Received authentication request packet from " + player.getName());

        boolean renewal = buf.readBoolean();

        // TODO remove override
        String mid = buf.readString();
        if (mid == null || mid.isEmpty()) {
            mid = player.getUuidAsString();
        }

        UserAuthResponse response = AuthClient.getClientToken(mid);
        LongshipLink.LOGGER.info("Response: " + response);
        LongshipLink.LOGGER.info("Mid: " + response.getMid());
        LongshipLink.LOGGER.info("SubKey: " + response.getSubKey());
        LongshipLink.LOGGER.info("PubKey: " + response.getPubKey());
        LongshipLink.LOGGER.info("ServerID: " + response.getServerID());
        LongshipLink.LOGGER.info("Token: " + response.getToken());

        LLAuthResponsePacket.send(player, response.getSubKey(), response.getPubKey(), response.getToken(), renewal);
    }

    public static void send(boolean renewal, String mid) {
        LongshipLink.LOGGER.info("Sending authentication response packet to server");

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(renewal);
        buf.writeString(mid); // TODO remove override

        try {
            ClientPlayNetworking.send(LLMessages.AUTH_REQUEST_ID, buf);
        } catch (Exception e) {
            LongshipLink.LOGGER.error("Error sending auth request: " + e.getMessage());
        }
    }
}
