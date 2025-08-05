package org.dimasik.liteauction;

import lombok.Getter;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;

public class UpdateChecker {
    private final JavaPlugin plugin;
    private final File pluginFile;
    private final String currentVersion = "v1.7.1";
    private final String changeLogUrl;
    private final String pluginUrl;

    public UpdateChecker(JavaPlugin plugin, File pluginFile) {
        this.plugin = plugin;
        this.pluginFile = pluginFile;
        this.changeLogUrl = "https://raw.githubusercontent.com/Dimasik201O/LiteAuction/master/change.log";
        this.pluginUrl = "https://github.com/Dimasik201O/LiteAuction/releases/latest/download/LiteAuction.jar";
    }

    public void checkForUpdates() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String latestVersion = getLatestVersion();
                plugin.getLogger().info("Проверка наличия обновлений...");
                if (isNewerVersion(latestVersion, currentVersion)) {
                    plugin.getLogger().info("Найдена новая версия: " + latestVersion + " (current: " + currentVersion + ")");
                    plugin.getLogger().info("Скачиваю обновление...");

                    if (downloadUpdate()) {
                        plugin.getLogger().info("Обновление скачано. Устанавливаю...");
                        installUpdate();
                    } else {
                        plugin.getLogger().warning("Ошибка скачивания обновления.");
                    }
                } else {
                    plugin.getLogger().info("У вас установлена последняя версия (" + currentVersion + ")");
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Ошибка обновления плагина: " + e.getMessage());
            }
        });
    }

    private String getLatestVersion() throws IOException {
        URL url = new URL(changeLogUrl);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
            String firstLine = reader.readLine();
            if (firstLine != null && firstLine.startsWith("LATEST_VERSION=")) {
                return firstLine.substring("LATEST_VERSION=".length()).trim();
            }
        }
        throw new IOException("Could not find LATEST_VERSION in change.log");
    }

    private boolean isNewerVersion(String newVersion, String currentVersion) {
        if (newVersion == null || currentVersion == null) return false;
        return newVersion.compareTo(currentVersion) > 0;
    }

    private boolean downloadUpdate() {
        try {
            URL website = new URL(pluginUrl);
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            File updateFile = new File(plugin.getDataFolder().getParent(), "LiteAuction-updated.jar");
            FileOutputStream fos = new FileOutputStream(updateFile);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            fos.close();
            return true;
        } catch (IOException e) {
            plugin.getLogger().warning("Ошибка при попытке скачать обновление плагина: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void installUpdate() {
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            File pluginFile = this.pluginFile;
            File updateFile = new File(plugin.getDataFolder().getParent(), "LiteAuction-updated.jar");

            try {
                Files.move(updateFile.toPath(), pluginFile.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                PluginManager pm = plugin.getServer().getPluginManager();
                pm.disablePlugin(plugin);
                pm.enablePlugin(pm.loadPlugin(pluginFile));
            } catch (Exception e) {
                plugin.getLogger().warning("Ошибка при попытке установить обновление плагина: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}