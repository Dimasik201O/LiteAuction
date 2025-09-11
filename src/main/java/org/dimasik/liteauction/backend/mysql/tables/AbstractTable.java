package org.dimasik.liteauction.backend.mysql.tables;

import com.zaxxer.hikari.HikariDataSource;

import java.util.concurrent.CompletableFuture;

public abstract class AbstractTable {
    protected final HikariDataSource dataSource;

    public AbstractTable(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public abstract CompletableFuture<Void> createTable();
}
