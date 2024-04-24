package codes.tyr.longshiplink.network;

import codes.tyr.longshiplink.LongshipLink;
import codes.tyr.longshiplink.network.packet.LLAuthRequestPacket;
import codes.tyr.longshiplink.network.packet.LLAuthResponsePacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;

public class LLMessages {
    public static final Identifier AUTH_REQUEST_ID = new Identifier(LongshipLink.MOD_ID, "auth-request");
    public static final Identifier AUTH_RESPONSE_ID = new Identifier(LongshipLink.MOD_ID, "auth-response");

    public static void registerC2SPackets() {
        //LongshipLink.LOGGER.info("Registering C2S packets");
        ServerPlayNetworking.registerGlobalReceiver(LLMessages.AUTH_REQUEST_ID, LLAuthRequestPacket::receive);
    }

    public static void registerS2CPackets() {
        //LongshipLink.LOGGER.info("Registering S2C packets");
        ClientPlayNetworking.registerGlobalReceiver(LLMessages.AUTH_RESPONSE_ID, LLAuthResponsePacket::receive);
    }
}
