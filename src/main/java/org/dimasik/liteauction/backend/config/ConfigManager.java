package org.dimasik.liteauction.backend.config;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.dimasik.liteauction.LiteAuction;
import org.dimasik.liteauction.backend.exceptions.UnsupportedConfigurationException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager {
    @Getter
    private static String ECONOMY_EDITOR;

    @Getter
    private static Map<String, String> CUSTOM_TAGS = new HashMap<>();

    private static Map<String, FileConfiguration> configCache = new HashMap<>();
    private static LiteAuction plugin;

    public static void init(LiteAuction plugin) {
        ConfigManager.plugin = plugin;
        loadConfig();
    }

    public static void loadConfig() {
        ECONOMY_EDITOR = getString("config.yml", "economy-editor", "StickEco");
        if (!ECONOMY_EDITOR.equalsIgnoreCase("StickEco") &&
                !ECONOMY_EDITOR.equalsIgnoreCase("Vault")) {
            throw new UnsupportedConfigurationException("Тип экономики не существует!");
        }

        CUSTOM_TAGS.clear();
        ConfigurationSection tagsSection = plugin.getConfig().getConfigurationSection("custom_tags");
        if (tagsSection != null) {
            for (String key : tagsSection.getKeys(false)) {
                CUSTOM_TAGS.put(key, tagsSection.getString(key));
            }
        }
    }

    private static FileConfiguration getCachedConfiguration(String filePath) {
        if (configCache.containsKey(filePath)) {
            return configCache.get(filePath);
        }

        File file = new File(plugin.getDataFolder(), filePath);
        if (!file.exists()) {
            plugin.saveResource(filePath, false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        configCache.put(filePath, config);
        return config;
    }

    public static String getString(String filePath, String path, String def) {
        FileConfiguration config = getCachedConfiguration(filePath);
        return config.getString(path, def);
    }

    public static int getInt(String filePath, String path, int def) {
        FileConfiguration config = getCachedConfiguration(filePath);
        return config.getInt(path, def);
    }

    public static boolean getBoolean(String filePath, String path, boolean def) {
        FileConfiguration config = getCachedConfiguration(filePath);
        return config.getBoolean(path, def);
    }

    public static List<String> getStringList(String filePath, String path) {
        FileConfiguration config = getCachedConfiguration(filePath);
        return config.getStringList(path);
    }

    public static List<Integer> getIntList(String filePath, String path) {
        FileConfiguration config = getCachedConfiguration(filePath);
        return config.getIntegerList(path);
    }

    public static double getDouble(String filePath, String path, double def) {
        FileConfiguration config = getCachedConfiguration(filePath);
        return config.getDouble(path, def);
    }

    public static long getLong(String filePath, String path, long def) {
        FileConfiguration config = getCachedConfiguration(filePath);
        return config.getLong(path, def);
    }

    public static void clearCache() {
        configCache.clear();
    }

    public static void reloadConfig(String filePath) {
        File file = new File(plugin.getDataFolder(), filePath);
        if (file.exists()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            configCache.put(filePath, config);
        } else {
            configCache.remove(filePath);
        }
    }
}