package codes.tyr.longshiplink.network.packet;

import codes.tyr.longshiplink.LongshipLink;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;

public class LLAuthResponsePacket {
    public static void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        LongshipLink.LOGGER.info("Received authentication response packet from server");
    }
}
