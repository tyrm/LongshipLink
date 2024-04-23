package codes.tyr.longshiplink.network.packet;

import codes.tyr.longshiplink.LongshipLink;
import codes.tyr.longshiplink.config.LLServerConfigs;
import codes.tyr.longshiplink.network.LLMessages;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class LLAuthRequestPacket {
    public static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        LongshipLink.LOGGER.info("Received authentication request packet from " + player.getName());

        LLAuthResponsePacket.send(player, LLServerConfigs.SUB_KEY, LLServerConfigs.PUB_KEY, "");
    }
}
