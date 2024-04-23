package codes.tyr.longshiplink.config;

import com.mojang.datafixers.util.Pair;
import codes.tyr.longshiplink.LongshipLink;

public class LLServerConfigs {
    public static SimpleConfig CONFIG;
    private static LLServerConfigProvider configs;

    public static String SUB_KEY;
    public static String PUB_KEY;
    public static String SEC_KEY;
    public static String SERVER_ID;

    public static void registerConfigs() {
        configs = new LLServerConfigProvider();
        createConfigs();

        CONFIG = SimpleConfig.of(LongshipLink.MOD_ID).provider(configs).request();

        assignConfigs();
    }

    private static void createConfigs() {
        configs.addKeyValuePair(new Pair<>("ll.server_id", "0000000000000000"), "Minecraft Server ID");
        configs.addKeyValuePair(new Pair<>("pubnub.subkey", "sub-c-00000000-0000-0000-0000-000000000000"), "PubNub Subscribe Key");
        configs.addKeyValuePair(new Pair<>("pubnub.pubkey", "pub-c-00000000-0000-0000-0000-000000000000"), "PubNub Publish Key");
        configs.addKeyValuePair(new Pair<>("pubnub.seckey", "sec-c-000000000000000000000000000000000000000000000000"), "PubNub Secret Key");
    }

    private static void assignConfigs() {
        SERVER_ID = CONFIG.getOrDefault("ll.server_id", null);
        SUB_KEY = CONFIG.getOrDefault("pubnub.subkey", null);
        PUB_KEY = CONFIG.getOrDefault("pubnub.pubkey", null);
        SEC_KEY = CONFIG.getOrDefault("pubnub.seckey", null);

        LongshipLink.LOGGER.info("All " + configs.getConfigsList().size() + " have been set properly");
    }
}
