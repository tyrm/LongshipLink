package codes.tyr.longshiplink.config;

import com.mojang.datafixers.util.Pair;
import codes.tyr.longshiplink.LongshipLink;

public class LLServerConfigs {
    public static SimpleConfig CONFIG;
    private static LLServerConfigProvider configs;
    public static String AUTH_URL;
    public static String SERVER_ID;
    public static String SERVER_SECRET;

    public static void registerConfigs() {
        configs = new LLServerConfigProvider();
        createConfigs();

        CONFIG = SimpleConfig.of(LongshipLink.MOD_ID).provider(configs).request();

        assignConfigs();
    }

    private static void createConfigs() {
        configs.addKeyValuePair(new Pair<>("auth.url", "http://localhost:5420"), "LongshipLink Auth URL");
        configs.addKeyValuePair(new Pair<>("server.id", "0000000000000000"), "LongshipLink Server ID");
        configs.addKeyValuePair(new Pair<>("server.secret", "00000000000000000000000000000000"), "LongshipLink Server Secret");
    }

    private static void assignConfigs() {
        AUTH_URL = CONFIG.getOrDefault("auth.url", null);
        SERVER_ID = CONFIG.getOrDefault("server.id", null);
        SERVER_SECRET = CONFIG.getOrDefault("server.secret", null);

        LongshipLink.LOGGER.info("All " + configs.getConfigsList().size() + " have been set properly");
    }
}
