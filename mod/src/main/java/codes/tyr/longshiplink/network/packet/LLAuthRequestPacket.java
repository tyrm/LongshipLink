package codes.tyr.longshiplink.network.packet;

import codes.tyr.longshiplink.LongshipLink;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class LLAuthRequestPacket {
    public static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        LongshipLink.LOGGER.info("Received authentication request packet from " + player.getName());
    }
}
