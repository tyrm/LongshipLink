package codes.tyr.longshiplink.pubnub;

import codes.tyr.longshiplink.LongshipLink;
import codes.tyr.longshiplink.LongshipLinkClient;
import codes.tyr.longshiplink.config.LLClientConfigs;
import codes.tyr.longshiplink.network.packet.LLAuthRequestPacket;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.UserId;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.enums.PNStatusCategory;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.objects_api.uuid.PNGetUUIDMetadataResult;
import com.pubnub.api.models.consumer.objects_api.uuid.PNSetUUIDMetadataResult;
import com.pubnub.api.models.consumer.presence.PNHereNowResult;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;
import com.pubnub.api.retry.RetryConfiguration;
import com.pubnub.api.v2.PNConfiguration;
import com.pubnub.api.v2.entities.Channel;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClientConnection {
    private PubNub pubnub;
    private String uid;
    private String username;
    private Character userColor;
    private final String GLOBAL_PREFIX = "☄";
    private final String TELL_PREFIX = "☇";
    private final Character DEFAULT_COLOR = '7';
    private ScheduledExecutorService authLoop;
    public ClientConnection() {
        pubnub = null;
    }
    public void close() {
        authLoop.shutdown();
        pubnub.unsubscribeAll();
    }
    public void setKeys(String uid, String subKey, String pubKey, String username) {
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
                if (pnStatus.getCategory().equals(PNStatusCategory.PNConnectionError)) {
                    String text = "missing exception";
                    if (pnStatus.getException() != null) {
                        text = pnStatus.getException().toString();
                    }
                    LongshipLink.LOGGER.error("PubNub connection error: " + text);
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
                try {
                    Gson gson = new Gson();
                    ChatMessage chatMessage = gson.fromJson(o.getMessage(), ChatMessage.class);
                    MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.of(GLOBAL_PREFIX + "§" + chatMessage.getUserColor() + "<" + chatMessage.getSender() + ">§r " + chatMessage.getMessage()));
                }
                catch (Exception e) {
                    LongshipLink.LOGGER.error("Error parsing message: " + e);
                }
            }

            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult pnMessageResult) {
                LongshipLink.LOGGER.info("PubNub presence " + pnMessageResult.getEvent() + ": " + pnMessageResult.getUuid()+"("+pnMessageResult.getOccupancy()+")");
                LongshipLink.LOGGER.info(pnMessageResult.getState());
                try {
                    Gson gson = new Gson();
                    UserState userState = gson.fromJson(pnMessageResult.getState(), UserState.class);
                    String event = pnMessageResult.getEvent();
                    if (event == null) {
                        return;
                    }

                    LongshipLink.LOGGER.info("Event: " + event);
                    switch (event) {
                        case "join":
                            if (!userState.getServerID().equals(LongshipLinkClient.serverID)) {
                                MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.of(GLOBAL_PREFIX + "§8§o" + userState.getUsername() + " joined the chat§r"));
                            }
                            break;
                        case "leave":
                            if (!userState.getServerID().equals(LongshipLinkClient.serverID)) {
                                MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.of(GLOBAL_PREFIX + "§8§o" + userState.getUsername() + " left the chat§r"));
                            }
                            break;
                        case "timeout":
                            if (!userState.getServerID().equals(LongshipLinkClient.serverID)) {
                                MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.of(GLOBAL_PREFIX + "§8§o" + userState.getUsername() + " timed out§r"));
                            }
                            break;
                        default:
                            break;
                    }
                }
                catch (Exception e) {
                    LongshipLink.LOGGER.error("Error parsing message: " + e);
                }
            }
        });
    }
    public void sendChatMessage(String message) {
        if (pubnub == null) {
            LongshipLink.LOGGER.error("PubNub not initialized");
            return;
        }

        Channel pubChan = pubnub.channel(PNChannel.GLOBAL_CHAT);
        pubChan.publish(new ChatMessage(username, message, LongshipLinkClient.serverID, userColor)).async((result) -> {
            if (result.isSuccess()) {
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
        pubnub.setToken(token);
    }
    public void doSubscribe(String username) {
        if (pubnub == null) {
            LongshipLink.LOGGER.error("PubNub not initialized");
            return;
        }

        List<String> chans = new ArrayList<>();
        chans.add(PNChannel.GLOBAL_CHAT);

        pubnub.setPresenceState().channels(chans).state(new UserState(username, LongshipLinkClient.serverID)).async((result) -> {
            if (!result.isSuccess()) {
                LongshipLink.LOGGER.error("Presence state error: " + result);
            }
        });
        pubnub.subscribe().channels(chans).withPresence().execute();
    }
    public void reconnect() {
        if (pubnub == null) {
            LongshipLink.LOGGER.error("PubNub not initialized");
            return;
        }

        pubnub.reconnect();
    }

    public void handleNewAuth(String mid, String subKey, String pubKey, String token, boolean renewal) {
        if (renewal) {
            setToken(token);
            reconnect();
        } else {
            String username = MinecraftClient.getInstance().getSession().getUsername();

            setKeys(mid, subKey, pubKey, username);
            setToken(token);
            doSubscribe(username);

            userColor = getUserColor();
        }
    }

    // Auth Loop
    public Runnable doReauth() {
        return () -> {
            LongshipLink.LOGGER.info("Re-authenticating with PubNub");

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
        authLoop = Executors.newScheduledThreadPool(1);
        authLoop.scheduleAtFixedRate(doReauth(), 60, 60, TimeUnit.MINUTES);
    }
    public void stopAuthLoop() {
        LongshipLink.LOGGER.info("Stopping PubNub auth loop");
        authLoop.shutdown();
    }

    // User Color
    public Character getUserColor() {
        try {
            PNGetUUIDMetadataResult result = pubnub.getUUIDMetadata().uuid(LongshipLinkClient.MID()).includeCustom(true).sync();
            Object data = result.getData().getCustom();
            Map<String, Object> map = (Map<String, Object>) data;

            if (map.containsKey("color")) {
                return ((String) map.get("color")).charAt(0);
            }
        } catch (PubNubException e) {
            LongshipLink.LOGGER.error("User metadata exception: " + e);
        } catch (ClassCastException e) {
            LongshipLink.LOGGER.error("User metadata not a map");
        }

        return DEFAULT_COLOR;
    }

    public void setUserColor(Character color) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("color", color);

        try {
            PNSetUUIDMetadataResult result = pubnub.setUUIDMetadata().uuid(LongshipLinkClient.MID()).custom(metadata).sync();
            userColor = color;

            MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.of("§" + color + "§oColor Updated§r"));

        } catch (PubNubException e) {
            LongshipLink.LOGGER.error("User metadata set exception: " + e);
        }
    }

    // Online Users
    public List<String> getOnlineUsers() {
        List<String> users = new ArrayList<>();

        List<String> chans = new ArrayList<>();
        chans.add(PNChannel.GLOBAL_CHAT);
        try {
            PNHereNowResult result = pubnub.hereNow().channels(chans).includeState(true).sync();
            result.getChannels().get(PNChannel.GLOBAL_CHAT).getOccupants().forEach((occupant) -> {
                JsonElement state = occupant.getState();
                if (state == null) {
                    return;
                }

                Gson gson = new Gson();
                UserState userState = gson.fromJson(state, UserState.class);
                if (userState.getUsername() != null) {
                    users.add(userState.getUsername());
                }
            });
        } catch (PubNubException e) {
            throw new RuntimeException(e);
        }

        return users;
    }
}
