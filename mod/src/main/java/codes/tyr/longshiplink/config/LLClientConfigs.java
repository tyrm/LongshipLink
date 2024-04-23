package codes.tyr.longshiplink.config;

import codes.tyr.longshiplink.LongshipLink;
import com.mojang.datafixers.util.Pair;

public class LLClientConfigs {
    public static SimpleConfig CONFIG;
    private static LLServerConfigProvider configs;

    public static String UUID;

    public static void registerConfigs() {
        configs = new LLServerConfigProvider();
        createConfigs();

        CONFIG = SimpleConfig.of(LongshipLink.MOD_ID+"-client").provider(configs).request();

        assignConfigs();
    }

    private static void createConfigs() {
        configs.addKeyValuePair(new Pair<>("pubnub.uuid", "00000000-0000-0000-0000-000000000000"), "Fake Minecraft UUID");
    }

    private static void assignConfigs() {
        UUID = CONFIG.getOrDefault("pubnub.uuid", null);

        LongshipLink.LOGGER.info("All " + configs.getConfigsList().size() + " have been set properly");
    }
}
