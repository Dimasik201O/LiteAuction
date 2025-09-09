package org.dimasik.liteauction.backend.mysql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import org.dimasik.liteauction.backend.mysql.impl.BidItems;
import org.dimasik.liteauction.backend.mysql.impl.Bids;
import org.dimasik.liteauction.backend.mysql.impl.GuiDatas;
import org.dimasik.liteauction.backend.mysql.impl.SellItems;
import org.dimasik.liteauction.backend.mysql.impl.Sounds;
import org.dimasik.liteauction.backend.mysql.impl.UnsoldItems;

import java.util.concurrent.CompletableFuture;

@Getter
public class DatabaseManager {
    private final HikariDataSource dataSource;
    private final SellItems sellItemsManager;
    private final UnsoldItems unsoldItemsManager;
    private final Sounds soundsManager;
    private final BidItems bidItemsManager;
    private final Bids bidsManager;
    private final GuiDatas guiDatasManager;

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
        this.bidItemsManager = new BidItems(dataSource);
        this.bidsManager = new Bids(dataSource);
        this.guiDatasManager = new GuiDatas(dataSource);
    }

    public CompletableFuture<Void> initialize() {
        return CompletableFuture.allOf(
                sellItemsManager.createTable(),
                unsoldItemsManager.createTable(),
                soundsManager.createTable(),
                bidItemsManager.createTable(),
                bidsManager.createTable(),
                guiDatasManager.createTable()
        );
    }

    public void moveExpiredItems() {
        sellItemsManager.moveExpiredItems();
        unsoldItemsManager.deleteExpiredItems();
        bidItemsManager.moveExpiredItems();
    }

    public void close() {
        dataSource.close();
    }
}