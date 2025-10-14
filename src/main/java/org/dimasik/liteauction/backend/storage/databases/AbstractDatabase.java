package org.dimasik.liteauction.backend.storage.databases;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.dimasik.liteauction.backend.storage.tables.impl.*;

import java.util.concurrent.CompletableFuture;

@Getter
@NoArgsConstructor
public abstract class AbstractDatabase {
    protected HikariDataSource dataSource;
    protected SellItems sellItemsManager;
    protected UnsoldItems unsoldItemsManager;
    protected Sounds soundsManager;
    protected BidItems bidItemsManager;
    protected Bids bidsManager;
    protected GuiDatas guiDatasManager;
    protected HistoryItems historyItems;

    public void initialize(HikariConfig config) {
        this.dataSource = new HikariDataSource(config);
        this.sellItemsManager = new SellItems(dataSource);
        this.unsoldItemsManager = new UnsoldItems(dataSource);
        this.soundsManager = new Sounds(dataSource);
        this.bidItemsManager = new BidItems(dataSource);
        this.bidsManager = new Bids(dataSource);
        this.guiDatasManager = new GuiDatas(dataSource);
        this.historyItems = new HistoryItems(dataSource);
    }

    public CompletableFuture<Void> createTables(){
        return CompletableFuture.allOf(
                sellItemsManager.createTable(),
                unsoldItemsManager.createTable(),
                soundsManager.createTable(),
                bidItemsManager.createTable(),
                bidsManager.createTable(),
                guiDatasManager.createTable(),
                historyItems.createTable()
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

    public String editQuery(String sql){
        return sql;
    }
}
