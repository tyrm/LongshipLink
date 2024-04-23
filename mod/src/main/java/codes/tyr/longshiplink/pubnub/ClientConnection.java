package codes.tyr.longshiplink.pubnub;

import codes.tyr.longshiplink.LongshipLink;
import codes.tyr.longshiplink.LongshipLinkClient;
import codes.tyr.longshiplink.config.LLClientConfigs;
import codes.tyr.longshiplink.network.packet.LLAuthRequestPacket;
import com.google.gson.Gson;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.UserId;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.enums.PNStatusCategory;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;
import com.pubnub.api.retry.RetryConfiguration;
import com.pubnub.api.v2.PNConfiguration;
import com.pubnub.api.v2.entities.Channel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClientConnection {
    private PubNub pubnub;
    private String uid;
    private String username;
    private final String GLOBAL_PREFIX = "☄";
    private final ScheduledExecutorService authLoop = Executors.newScheduledThreadPool(1);
    public ClientConnection() {
        pubnub = null;
    }
    public void close() {
        authLoop.shutdown();
        pubnub.unsubscribeAll();
    }
    public void setKeys(String uid, String subKey, String pubKey, String username) {
        LongshipLink.LOGGER.info("Got SubKey: " + subKey);
        LongshipLink.LOGGER.info("Got PubKey: " + pubKey);
        LongshipLink.LOGGER.info("Setting PubNub keys for user " + uid);

        PNConfiguration pnConfiguration;

        // TODO: Remove this when the mcID is properly set
        if (LLClientConfigs.UUID != null && !LLClientConfigs.UUID.equals("00000000-0000-0000-0000-000000000000")) {
            LongshipLink.LOGGER.info("Using faux UUID: " + LLClientConfigs.UUID);
            uid = LLClientConfigs.UUID;
        }
        this.uid = uid;
        this.username = username;

        try {
            final UserId userId = new UserId(uid);
            pnConfiguration = PNConfiguration.builder(userId, subKey)
                .publishKey(pubKey)
                .retryConfiguration(new RetryConfiguration.Exponential())
                //.logVerbosity(PNLogVerbosity.BODY)
                .build();
        } catch (PubNubException e) {
            throw new RuntimeException(e);
        }
        pubnub = PubNub.create(pnConfiguration);
        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus pnStatus) {
                LongshipLink.LOGGER.info("PubNub status: UID " + pnStatus.getCategory());

                if (pnStatus.getCategory().equals(PNStatusCategory.PNConnectionError)) {
                    String text = "missing exception";
                    if (pnStatus.getException() != null) {
                        text = pnStatus.getException().toString();
                    }
                    LongshipLink.LOGGER.error("PubNub connection error: " + text);
                } else if (pnStatus.getCategory().equals(PNStatusCategory.PNConnectedCategory)) {
                    String text = "missing exception";
                    if (pnStatus.getException() != null) {
                        text = pnStatus.getException().toString();
                    }
                    LongshipLink.LOGGER.info("PubNub connected: " + text);
                } else if (pnStatus.getCategory().equals(PNStatusCategory.PNUnexpectedDisconnectCategory)) {
                    String text = "missing exception";
                    if (pnStatus.getException() != null) {
                        text = pnStatus.getException().toString();
                    }
                    LongshipLink.LOGGER.info("PubNub unexpectedly disconnected: "+text);
                } else if (pnStatus.getCategory().equals(PNStatusCategory.PNDisconnectedCategory)) {
                    String text = "missing exception";
                    if (pnStatus.getException() != null) {
                        text = pnStatus.getException().toString();
                    }
                    LongshipLink.LOGGER.info("PubNub disconnected: " + text);
                }
            }
            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult o) {
                LongshipLink.LOGGER.info("PubNub message: " + o.getMessage());

                try {
                    Gson gson = new Gson();
                    ChatMessage chatMessage = gson.fromJson(o.getMessage(), ChatMessage.class);

                    LongshipLink.LOGGER.info("Chat message from "+chatMessage.getServerID()+": " + chatMessage.getSender() + "(" + o.getPublisher() + "): " + chatMessage.getMessage());
                    MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.of(GLOBAL_PREFIX + "§7<" + chatMessage.getSender() + ">§r " + chatMessage.getMessage()));
                }
                catch (Exception e) {
                    LongshipLink.LOGGER.error("Error parsing message: " + e);
                }
            }

            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult pnMessageResult) {
                LongshipLink.LOGGER.info("PubNub presence: " + pnMessageResult);
                LongshipLink.LOGGER.info("PubNub presence event: " + pnMessageResult.getEvent());
                LongshipLink.LOGGER.info("PubNub presence UUID: " + pnMessageResult.getUuid());
            }
        });
    }
    public void sendChatMessage(String message) {
        if (pubnub == null) {
            LongshipLink.LOGGER.error("PubNub not initialized");
            return;
        }

        Channel pubChan = pubnub.channel(PNChannel.GLOBAL_CHAT);
        pubChan.publish(new ChatMessage(username, message, LongshipLinkClient.serverID)).async((result) -> {
            if (result.isSuccess()) {
                LongshipLink.LOGGER.info("Message published: " + result);
                return;
            }

            LongshipLink.LOGGER.error("Message publish error: " + result);
            PubNubException ex = result.exceptionOrNull();
            if (ex == null) {
                LongshipLink.LOGGER.error("No exception found");
            }
        });
    }
    public void setToken(String token) {
        LongshipLink.LOGGER.info("Setting PubNub token");
        pubnub.setToken(token);
    }
    public void doSubscribe(String username) {
        if (pubnub == null) {
            LongshipLink.LOGGER.error("PubNub not initialized");
            return;
        }

        List<String> chans = new ArrayList<>();
        chans.add(PNChannel.GLOBAL_CHAT);
        pubnub.subscribe().channels(chans).withPresence().execute();

        pubnub.setPresenceState().channels(chans).state(new UserState(username, LongshipLinkClient.serverID)).async((result) -> {
            if (result.isSuccess()) {
                LongshipLink.LOGGER.info("Presence state set: " + result);
            } else {
                LongshipLink.LOGGER.error("Presence state error: " + result);
            }
        });
    }
    public Runnable doReauth() {
        return () -> {
            LongshipLink.LOGGER.info("Reauthenticating with PubNub");

            // TODO: Remove this override. Server knows minecraft uuid.
            String mid = MinecraftClient.getInstance().getSession().getUuidOrNull().toString();
            if (LLClientConfigs.UUID != null && !LLClientConfigs.UUID.equals("00000000-0000-0000-0000-000000000000")) {
                LongshipLink.LOGGER.warn("Using faux UUID: " + LLClientConfigs.UUID);
                mid = LLClientConfigs.UUID;
            }
            LLAuthRequestPacket.send(true, mid);
        };
    }
    public void startAuthLoop() {
        // TODO: Remove this override. Server knows minecraft uuid.
        String mid = MinecraftClient.getInstance().getSession().getUuidOrNull().toString();
        if (LLClientConfigs.UUID != null && !LLClientConfigs.UUID.equals("00000000-0000-0000-0000-000000000000")) {
            LongshipLink.LOGGER.warn("Using faux UUID: " + LLClientConfigs.UUID);
            mid = LLClientConfigs.UUID;
        }

        LLAuthRequestPacket.send(false, mid);

        // Start reauth loop
        authLoop.scheduleAtFixedRate(doReauth(), 60, 60, TimeUnit.MINUTES);
    }
    public void reconnect() {
        if (pubnub == null) {
            LongshipLink.LOGGER.error("PubNub not initialized");
            return;
        }

        LongshipLink.LOGGER.info("Reconnecting PubNub");
        pubnub.reconnect();
    }

    public void stopAuthLoop() {
        LongshipLink.LOGGER.info("Stopping PubNub auth loop");
        authLoop.shutdown();
    }
}
