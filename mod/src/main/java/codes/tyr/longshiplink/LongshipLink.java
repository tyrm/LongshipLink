package codes.tyr.longshiplink;

import codes.tyr.longshiplink.config.LLServerConfigs;
import codes.tyr.longshiplink.event.LLChatMessageServerHandler;
import codes.tyr.longshiplink.network.LLMessages;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class LongshipLink implements ModInitializer {
    public static final String MOD_ID = "longship-link";
    public static final Logger LOGGER = LogManager.getLogger("LongshipLink");
    public static final List<String> COLORS = Arrays.asList(
            "black",
            "dark_blue",
            "dark_green",
            "dark_aqua",
            "dark_red",
            "dark_purple",
            "gold",
            "gray",
            "dark_gray",
            "blue",
            "green",
            "aqua",
            "red",
            "light_purple",
            "yellow",
            "white"
    );
    public static final Map<String, Character> COLOR_CODES = Map.ofEntries(
            Map.entry("black", '0'),
            Map.entry("dark_blue", '1'),
            Map.entry("dark_green", '2'),
            Map.entry("dark_aqua", '3'),
            Map.entry("dark_red", '4'),
            Map.entry("dark_purple", '5'),
            Map.entry("gold", '6'),
            Map.entry("gray", '7'),
            Map.entry("dark_gray", '8'),
            Map.entry("blue", '9'),
            Map.entry("green", 'a'),
            Map.entry("aqua", 'b'),
            Map.entry("red", 'c'),
            Map.entry("light_purple", 'd'),
            Map.entry("yellow", 'e'),
            Map.entry("white", 'f')
    );
    @Override
    public void onInitialize() {
        LOGGER.info("Initializing LongshipLink");

        LLServerConfigs.registerConfigs(); // must be called first

        LLMessages.registerC2SPackets();

        //LLChatMessageServerHandler.register();
    }
}
