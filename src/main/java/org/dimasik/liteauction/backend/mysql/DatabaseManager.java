package org.dimasik.liteauction.backend.mysql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.dimasik.liteauction.backend.config.ConfigManager;
import org.dimasik.liteauction.backend.mysql.impl.SellItems;
import org.dimasik.liteauction.backend.mysql.impl.Sounds;
import org.dimasik.liteauction.backend.mysql.impl.UnsoldItems;

import java.util.concurrent.CompletableFuture;

public class DatabaseManager {
    private final HikariDataSource dataSource;
    @lombok.Getter
    private final SellItems sellItemsManager;
    @lombok.Getter
    private final UnsoldItems unsoldItemsManager;
    @lombok.Getter
    private final Sounds soundsManager;

    public DatabaseManager(String host, String username, String password, String database) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + "/" + database);
        config.setUsername(username);
        config.setPassword(password);
        config.addDataSourceProperty("autoReconnect", "true");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtsCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        this.dataSource = new HikariDataSource(config);

        this.sellItemsManager = new SellItems(dataSource);
        this.unsoldItemsManager = new UnsoldItems(dataSource);
        this.soundsManager = new Sounds(dataSource);
    }

    public CompletableFuture<Void> initialize() {
        return CompletableFuture.allOf(
                sellItemsManager.createTable(),
                unsoldItemsManager.createTable(),
                soundsManager.createTable()
        );
    }

    public void moveExpiredItems() {
        sellItemsManager.moveExpiredItems();
        unsoldItemsManager.deleteExpiredItems();
    }

    public void close() {
        dataSource.close();
    }
}