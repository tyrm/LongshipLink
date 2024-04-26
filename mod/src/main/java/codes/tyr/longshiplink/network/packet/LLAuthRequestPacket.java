package codes.tyr.longshiplink.network.packet;

import codes.tyr.longshiplink.LongshipLink;
import codes.tyr.longshiplink.LongshipLinkClient;
import codes.tyr.longshiplink.auth.AuthClient;
import codes.tyr.longshiplink.auth.UserAuthResponse;
import codes.tyr.longshiplink.config.LLServerConfigs;
import codes.tyr.longshiplink.network.LLMessages;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class LLAuthRequestPacket {
    public static void receive(MinecraftServer server, @NotNull ServerPlayerEntity player, ServerPlayNetworkHandler handler, @NotNull PacketByteBuf buf, PacketSender responseSender) {
        boolean renewal = buf.readBoolean();
        String reqMID = buf.readString();
        String mid = player.getUuidAsString();

        if (LLServerConfigs.validateMid() && !reqMID.equals(mid)) {
            LongshipLink.LOGGER.warn("Invalid MID: " + reqMID + " != " + mid);
            return;
        } else {
            if (reqMID != null && !reqMID.isEmpty()) {
                LongshipLink.LOGGER.warn("Using faux MID: " + reqMID);
                mid = reqMID;
            }
        }

        try {
            UserAuthResponse response = AuthClient.getClientToken(mid);
            LLAuthResponsePacket.send(player, mid, response.getSubKey(), response.getPubKey(), response.getToken(), renewal);
        } catch (IOException e) {
            LongshipLink.LOGGER.error("Error getting client token: " + e.getMessage());
        }
    }

    public static void send(boolean renewal) {
        String mid = LongshipLinkClient.MID();

        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(renewal);
        buf.writeString(mid);

        try {
            ClientPlayNetworking.send(LLMessages.AUTH_REQUEST_ID, buf);
        } catch (Exception e) {
            LongshipLink.LOGGER.error("Error sending auth request: " + e.getMessage());
        }
    }
}
