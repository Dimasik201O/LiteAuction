package org.dimasik.liteauction.backend.config;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.dimasik.liteauction.LiteAuction;
import org.dimasik.liteauction.backend.enums.BidsSortingType;
import org.dimasik.liteauction.backend.enums.MarketSortingType;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    @Getter
    private static String MYSQL_HOST;
    @Getter
    private static String MYSQL_USER;
    @Getter
    private static String MYSQL_PASSWORD;
    @Getter
    private static String MYSQL_DATABASE;

    @Getter
    private static String REDIS_HOST;
    @Getter
    private static int REDIS_PORT;
    @Getter
    private static String REDIS_PASSWORD;
    @Getter
    private static String REDIS_CHANNEL;

    @Getter
    private static boolean IS_HEAD;
    @Getter
    private static int DEFAULT_AUTO_PRICE;
    @Getter
    private static String ECONOMY_EDITOR;

    @Getter
    private static MarketSortingType defaultMarketSortingType = MarketSortingType.CHEAPEST_FIRST;
    @Getter
    private static BidsSortingType defaultBidsSortingType = BidsSortingType.CHEAPEST_FIRST;

    @Getter
    private static Map<String, String> CUSTOM_TAGS = new HashMap<>();

    private static FileConfiguration config;

    public static void init(FileConfiguration config) {
        ConfigManager.config = config;
        loadConfig();
    }

    public static void loadConfig() {
        MYSQL_HOST = config.getString("mysql.host", "localhost");
        MYSQL_USER = config.getString("mysql.user", "root");
        MYSQL_PASSWORD = config.getString("mysql.password", "сайнес гпт кодер");
        MYSQL_DATABASE = config.getString("mysql.database", "lite_auction");

        REDIS_HOST = config.getString("redis.host", "localhost");
        REDIS_PORT = config.getInt("redis.port", 6379);
        REDIS_PASSWORD = config.getString("redis.password", "сайнес гпт кодер");
        REDIS_CHANNEL = config.getString("redis.channel", "auction");

        IS_HEAD = config.getBoolean("isHead", true);
        DEFAULT_AUTO_PRICE = config.getInt("default-auto-price", 500);
        ECONOMY_EDITOR = config.getString("economy-editor", "StickEco");
        if(!ECONOMY_EDITOR.equalsIgnoreCase("StickEco") && !ECONOMY_EDITOR.equalsIgnoreCase("Vault")){
            ECONOMY_EDITOR = "StickEco";
        }

        loadCustomTags();
    }

    private static void loadCustomTags() {
        CUSTOM_TAGS.clear();
        ConfigurationSection tagsSection = config.getConfigurationSection("custom_tags");
        if (tagsSection != null) {
            for (String key : tagsSection.getKeys(false)) {
                CUSTOM_TAGS.put(key, tagsSection.getString(key));
            }
        }
    }

    public static String getString(String path, String def) {
        return config.getString(path, def);
    }

    public static int getInt(String path, int def) {
        return config.getInt(path, def);
    }
}