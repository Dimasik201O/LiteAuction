package org.dimasik.liteauction.backend.storage.databases.impl;

import com.zaxxer.hikari.HikariConfig;
import org.dimasik.liteauction.LiteAuction;
import org.dimasik.liteauction.backend.config.ConfigManager;
import org.dimasik.liteauction.backend.storage.databases.AbstractDatabase;

import java.io.File;

public class SQLite extends AbstractDatabase {
    public SQLite(String databaseName) {
        super();
        File dbFile = new File(LiteAuction.getInstance().getDataFolder(), databaseName);
        File parentDir = dbFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
        config.setUsername("");
        config.setPassword("");
        config.setMaximumPoolSize(10    );
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.addDataSourceProperty("foreign_keys", "true");
        config.addDataSourceProperty("journal_mode", "WAL");
        config.addDataSourceProperty("synchronous", "NORMAL");
        super.initialize(config);
    }

    @Override
    public String editQuery(String sql){
        String dbType = ConfigManager.getDATABASE_TYPE();
        if (dbType.equalsIgnoreCase("SQLite")) {
            sql = sql.replaceAll("INT AUTO_INCREMENT PRIMARY KEY", "INTEGER PRIMARY KEY")
                    .replaceAll("INT NOT NULL", "INTEGER NOT NULL");
        }
        return sql;
    }
}