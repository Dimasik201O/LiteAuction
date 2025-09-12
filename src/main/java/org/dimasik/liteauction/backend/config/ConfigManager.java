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
    private static String COMMUNICATION_TYPE;

    @Getter
    private static String REDIS_HOST;
    @Getter
    private static int REDIS_PORT;
    @Getter
    private static String REDIS_PASSWORD;
    @Getter
    private static String REDIS_CHANNEL;

    @Getter
    private static String RABBITMQ_HOST;
    @Getter
    private static int RABBITMQ_PORT;
    @Getter
    private static String RABBITMQ_VHOST;
    @Getter
    private static String RABBITMQ_USER;
    @Getter
    private static String RABBITMQ_PASSWORD;
    @Getter
    private static String RABBITMQ_CHANNEL;

    @Getter
    private static String[] NATS_HOST;
    @Getter
    private static String NATS_USER;
    @Getter
    private static String NATS_PASSWORD;
    @Getter
    private static String NATS_CHANNEL;

    @Getter
    private static String WEBSOCKET_HOST;
    @Getter
    private static int WEBSOCKET_PORT;
    @Getter
    private static String WEBSOCKET_PASSWORD;
    @Getter
    private static String WEBSOCKET_CHANNEL;

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

        COMMUNICATION_TYPE = config.getString("communication.type", "Redis");
        if(
                !COMMUNICATION_TYPE.equalsIgnoreCase("Redis") &&
                !COMMUNICATION_TYPE.equalsIgnoreCase("RabbitMQ") &&
                !COMMUNICATION_TYPE.equalsIgnoreCase("Nats") &&
                !COMMUNICATION_TYPE.equalsIgnoreCase("WebSocket") &&
                !COMMUNICATION_TYPE.equalsIgnoreCase("Local")
        ){
            throw new UnsupportedConfigurationException("Тип коммуникации не существует!");
        }

        REDIS_HOST = config.getString("communication.redis.host", "localhost");
        REDIS_PORT = config.getInt("communication.redis.port", 6379);
        REDIS_PASSWORD = config.getString("communication.redis.password", "сайнес гпт кодер");
        REDIS_CHANNEL = config.getString("communication.redis.channel", "auction");

        RABBITMQ_HOST = config.getString("communication.rabbitmq.host", "localhost");
        RABBITMQ_PORT = config.getInt("communication.rabbitmq.port", 5672);
        RABBITMQ_VHOST = config.getString("communication.rabbitmq.vhost", "/");
        RABBITMQ_USER = config.getString("communication.rabbitmq.user", "root");
        RABBITMQ_PASSWORD = config.getString("communication.global.password", "сайнес гпт кодер");
        RABBITMQ_CHANNEL = config.getString("communication.global.channel", "auction");

        NATS_HOST = config.getStringList("communication.nats.host").toArray(new String[]{});
        NATS_USER = config.getString("communication.nats.port", "root");
        NATS_PASSWORD = config.getString("communication.nats.password", "сайнес гпт кодер");
        NATS_CHANNEL = config.getString("communication.nats.channel", "auction");

        WEBSOCKET_HOST = config.getString("communication.websocket.host", "localhost");
        WEBSOCKET_PORT = config.getInt("communication.websocket.port", 6379);
        WEBSOCKET_PASSWORD = config.getString("communication.websocket.password", "сайнес гпт кодер");
        WEBSOCKET_CHANNEL = config.getString("communication.websocket.channel", "auction");

        IS_HEAD = config.getBoolean("isHead", true);
        DEFAULT_AUTO_PRICE = config.getInt("default-auto-price", 500);

        ECONOMY_EDITOR = config.getString("economy-editor", "StickEco");
        if(
                !ECONOMY_EDITOR.equalsIgnoreCase("StickEco") &&
                !ECONOMY_EDITOR.equalsIgnoreCase("Vault")
        ){
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