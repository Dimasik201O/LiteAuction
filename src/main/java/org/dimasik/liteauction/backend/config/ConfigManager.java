package org.dimasik.liteauction.backend.config;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.dimasik.liteauction.backend.exceptions.UnsupportedConfigurationException;

import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    @Getter
    private static String DATABASE_TYPE;
    @Getter
    private static String GLOBAL_HOST;
    @Getter
    private static String GLOBAL_USER;
    @Getter
    private static String GLOBAL_PASSWORD;
    @Getter
    private static String GLOBAL_DATABASE;
    @Getter
    private static String LOCAL_FILE;

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
    private static Map<String, String> CUSTOM_TAGS = new HashMap<>();

    private static FileConfiguration config;

    public static void init(FileConfiguration config) {
        ConfigManager.config = config;
        loadConfig();
    }

    public static void loadConfig() throws UnsupportedConfigurationException {
        DATABASE_TYPE = config.getString("database.type", "MySQL");
        if(
                !DATABASE_TYPE.equalsIgnoreCase("MySQL") &&
                !DATABASE_TYPE.equalsIgnoreCase("SQLite")
        ){
            throw new UnsupportedConfigurationException("Тип базы данных не существует!");
        }

        GLOBAL_HOST = config.getString("database.global.host", "localhost");
        GLOBAL_USER = config.getString("database.global.user", "root");
        GLOBAL_PASSWORD = config.getString("database.global.password", "сайнес гпт кодер");
        GLOBAL_DATABASE = config.getString("database.global.database", "lite_auction");

        LOCAL_FILE = config.getString("database.local.name", "database.db");

        REDIS_HOST = config.getString("redis.host", "localhost");
        REDIS_PORT = config.getInt("redis.port", 6379);
        REDIS_PASSWORD = config.getString("redis.password", "сайнес гпт кодер");
        REDIS_CHANNEL = config.getString("redis.channel", "auction");

        IS_HEAD = config.getBoolean("isHead", true);
        DEFAULT_AUTO_PRICE = config.getInt("default-auto-price", 500);

        ECONOMY_EDITOR = config.getString("economy-editor", "StickEco");
        if(!ECONOMY_EDITOR.equalsIgnoreCase("StickEco") && !ECONOMY_EDITOR.equalsIgnoreCase("Vault")){
            throw new UnsupportedConfigurationException("Тип экономики не существует!");
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