package codes.tyr.longshiplink.network.packet;

import codes.tyr.longshiplink.LongshipLink;
import codes.tyr.longshiplink.LongshipLinkClient;
import codes.tyr.longshiplink.config.LLServerConfigs;
import codes.tyr.longshiplink.network.LLMessages;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

public class LLAuthResponsePacket {
    public static void receive(MinecraftClient client, ClientPlayNetworkHandler handler, @NotNull PacketByteBuf buf, PacketSender responseSender) {
        LongshipLink.LOGGER.info("Received authentication response packet from client");

        String mid = buf.readString();
        String subKey = buf.readString();
        String pubKey = buf.readString();
        String serverID = buf.readString();
        String token = buf.readString();
        boolean renewal = buf.readBoolean();

        LongshipLinkClient.serverID = serverID;
        LongshipLinkClient.pn.handleNewAuth(mid, subKey, pubKey, token, renewal);
    }

    public static void send(@NotNull ServerPlayerEntity player, @NotNull String subKey, @NotNull String pubKey, @NotNull String token, boolean renewal) {
        LongshipLink.LOGGER.info("Sending authentication response packet to client");

        PacketByteBuf buf = PacketByteBufs.create();

        buf.writeString(player.getUuidAsString());
        buf.writeString(subKey);
        buf.writeString(pubKey);
        buf.writeString(LLServerConfigs.SERVER_ID);
        buf.writeString(token);
        buf.writeBoolean(renewal);

        try {
            ServerPlayNetworking.send(player, LLMessages.AUTH_RESPONSE_ID, buf);
        } catch (Exception e) {
            LongshipLink.LOGGER.error("Error sending auth request: " + e.getMessage());
        }
    }
}
