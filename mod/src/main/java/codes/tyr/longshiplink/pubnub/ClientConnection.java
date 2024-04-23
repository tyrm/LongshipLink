package codes.tyr.longshiplink.pubnub;

import codes.tyr.longshiplink.LongshipLink;
import codes.tyr.longshiplink.LongshipLinkClient;
import codes.tyr.longshiplink.config.LLClientConfigs;
import codes.tyr.longshiplink.config.LLServerConfigs;
import com.google.gson.Gson;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.UserId;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;
import com.pubnub.api.models.consumer.pubsub.PNSignalResult;
import com.pubnub.api.models.consumer.pubsub.files.PNFileEventResult;
import com.pubnub.api.models.consumer.pubsub.message_actions.PNMessageActionResult;
import com.pubnub.api.v2.PNConfiguration;
import com.pubnub.api.v2.entities.Channel;
import com.pubnub.api.v2.subscriptions.Subscription;
import com.pubnub.api.v2.subscriptions.SubscriptionOptions;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ClientConnection {
    private PubNub pubnub;
    private String userID;

    public ClientConnection() {
        pubnub = null;
    }

    public void close() {
        pubnub.unsubscribeAll();
    }

    public void setKeys(String uid, String subKey, String pubKey, String username) {
        LongshipLink.LOGGER.info("Got SubKey: " + subKey);
        LongshipLink.LOGGER.info("Got PubKey: " + pubKey);
        LongshipLink.LOGGER.info("Setting PubNub keys for user " + uid);

        PNConfiguration pnConfiguration;

        if (LLClientConfigs.UUID != null && !LLClientConfigs.UUID.equals("00000000-0000-0000-0000-000000000000")) {
            LongshipLink.LOGGER.info("Using faux UUID: " + LLClientConfigs.UUID);
            uid = LLClientConfigs.UUID;
        }
        userID = uid;

        try {
             final UserId userId = new UserId(uid);
             pnConfiguration = PNConfiguration.builder(userId, subKey)
                    .publishKey(pubKey)
                    .build();
        } catch (PubNubException e) {
            throw new RuntimeException(e);
        }

        pubnub = PubNub.create(pnConfiguration);
        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus pnStatus) {
                LongshipLink.LOGGER.info("PubNub status: " + pnStatus);
            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult o) {
                LongshipLink.LOGGER.info("PubNub message: " + o.getMessage());

                Gson gson = new Gson();
                ChatMessage chatMessage = gson.fromJson(o.getMessage(), ChatMessage.class);

                LongshipLink.LOGGER.info("Chat message from "+chatMessage.getServerID()+": " + chatMessage.getSender() + "(" + o.getPublisher() + "): " + chatMessage.getMessage());
                MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.of("☄§7<" + chatMessage.getSender() + ">§r " + chatMessage.getMessage()));
            }

            @Override
            public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult pnMessageResult) {
                LongshipLink.LOGGER.info("PubNub signal: " + pnMessageResult);
            }

            @Override
            public void messageAction(@NotNull PubNub pubnub, @NotNull PNMessageActionResult pnMessageResult) {
                LongshipLink.LOGGER.info("PubNub message action: " + pnMessageResult);
            }

            @Override
            public void file(@NotNull PubNub pubnub, @NotNull PNFileEventResult pnMessageResult) {
                LongshipLink.LOGGER.info("PubNub file: " + pnMessageResult);
            }

            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult pnMessageResult) {
                LongshipLink.LOGGER.info("PubNub presence: " + pnMessageResult);
            }
        });

        LongshipLink.LOGGER.info("Subscribing to PubNub");

        List<String> chans = new ArrayList<>();
        chans.add("global");
        pubnub.subscribe().channels(chans).withPresence().execute();

        pubnub.setPresenceState().channels(chans).state(new UserState(username, LongshipLinkClient.serverID)).async((result) -> {
            if (result.isSuccess()) {
                LongshipLink.LOGGER.info("Presence state set: " + result);
            } else {
                LongshipLink.LOGGER.error("Presence state error: " + result);
            }
        });
    }

    public void sendChatMessage(String username, String message) {
        if (pubnub == null) {
            LongshipLink.LOGGER.error("PubNub not initialized");
            return;
        }

        Channel pubChan = pubnub.channel("global");
        pubChan.publish(new ChatMessage(username, message, LongshipLinkClient.serverID)).async((result) -> {
            if (result.isSuccess()) {
                LongshipLink.LOGGER.info("Message published: " + result);
            } else {
                LongshipLink.LOGGER.error("Message publish error: " + result);
            }
        });
    }
}
