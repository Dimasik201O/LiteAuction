package org.dimasik.liteauction.backend.mysql.databases.impl;

import com.zaxxer.hikari.HikariConfig;
import lombok.Getter;
import org.dimasik.liteauction.backend.mysql.databases.AbstractDatabase;

@Getter
public class MysqlManager extends AbstractDatabase {
    public MysqlManager(String host, String username, String password, String database) {
        super();
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + "/" + database);
        config.setUsername(username);
        config.setPassword(password);
        config.addDataSourceProperty("autoReconnect", "true");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtsCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        super.initialize(config);
        super.createTables();
    }
}