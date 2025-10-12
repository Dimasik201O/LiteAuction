package org.dimasik.liteauction.backend.storage.tables.impl;

import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.inventory.ItemStack;
import org.dimasik.liteauction.LiteAuction;
import org.dimasik.liteauction.backend.config.ConfigManager;
import org.dimasik.liteauction.backend.config.utils.ConfigUtils;
import org.dimasik.liteauction.backend.enums.CategoryType;
import org.dimasik.liteauction.backend.enums.MarketSortingType;
import org.dimasik.liteauction.backend.storage.tables.AbstractTable;
import org.dimasik.liteauction.backend.storage.models.SellItem;
import org.dimasik.liteauction.backend.utils.tags.ItemHoverUtil;
import org.dimasik.liteauction.backend.utils.format.Parser;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class SellItems extends AbstractTable {
    public SellItems(HikariDataSource dataSource) {
        super(dataSource);
    }

    public CompletableFuture<Void> createTable() {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement()) {
                String sql = "CREATE TABLE IF NOT EXISTS sell_items (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "player VARCHAR(16) NOT NULL, " +
                        "itemstack TEXT NOT NULL, " +
                        "tags TEXT, " +
                        "price INT NOT NULL, " +
                        "amount INT NOT NULL, " +
                        "by_one BOOLEAN NOT NULL, " +
                        "create_time BIGINT NOT NULL)";

                sql = LiteAuction.getInstance().getDatabaseManager().editQuery(sql);

                statement.execute(sql);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Integer> addItem(String player, String itemStack, Set<String> tags, int price, int amount, boolean byOne) {
        return CompletableFuture.supplyAsync(() -> {
            long createTime = System.currentTimeMillis();
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "INSERT INTO sell_items (player, itemstack, tags, price, amount, by_one, create_time) " +
                                 "VALUES (?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {

                statement.setString(1, player);
                statement.setString(2, itemStack);
                statement.setString(3, String.join(",", tags));
                statement.setInt(4, price);
                statement.setInt(5, amount);
                statement.setBoolean(6, byOne);
                statement.setLong(7, createTime);

                int affectedRows = statement.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Creating item failed, no rows affected.");
                }

                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                    throw new SQLException("Creating item failed, no ID obtained.");
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<List<SellItem>> getAllItems(int page, int pageSize) {
        return CompletableFuture.supplyAsync(() -> {
            List<SellItem> items = new ArrayList<>();
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT * FROM sell_items ORDER BY id ASC LIMIT ? OFFSET ?")) {

                statement.setInt(1, pageSize);
                statement.setInt(2, (page - 1) * pageSize);

                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        items.add(extractSellItemFromResultSet(rs));
                    }
                    return items;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Integer> getAllItemsCount() {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM sell_items")) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Optional<SellItem>> getItemById(int id) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT * FROM sell_items WHERE id = ?")) {

                statement.setInt(1, id);
                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(extractSellItemFromResultSet(rs));
                    }
                    return Optional.empty();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<List<SellItem>> getPlayerItems(String player, int page, int pageSize) {
        return CompletableFuture.supplyAsync(() -> {
            List<SellItem> items = new ArrayList<>();
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT * FROM sell_items WHERE player = ? ORDER BY create_time DESC LIMIT ? OFFSET ?")) {
                statement.setString(1, player);
                statement.setInt(2, pageSize);
                statement.setInt(3, (page - 1) * pageSize);
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        items.add(extractSellItemFromResultSet(rs));
                    }
                    return items;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Integer> getPlayerItemsCount(String player) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT COUNT(*) FROM sell_items WHERE player = ?")) {
                statement.setString(1, player);
                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                    return 0;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<List<SellItem>> getExpiredPlayerItems(String player, int page, int pageSize) {
        return CompletableFuture.supplyAsync(() -> {
            List<SellItem> items = new ArrayList<>();
            long twelveHoursAgo = System.currentTimeMillis() - (ConfigManager.getLong("settings/settings.yml", "lifetime.sell", 43200) * 1000);
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT * FROM sell_items WHERE player = ? AND create_time < ? LIMIT ? OFFSET ?")) {

                statement.setString(1, player);
                statement.setLong(2, twelveHoursAgo);
                statement.setInt(3, pageSize);
                statement.setInt(4, (page - 1) * pageSize);
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        items.add(extractSellItemFromResultSet(rs));
                    }
                    return items;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Integer> getExpiredPlayerItemsCount(String player) {
        return CompletableFuture.supplyAsync(() -> {
            long twelveHoursAgo = System.currentTimeMillis() - (ConfigManager.getLong("settings/settings.yml", "lifetime.sell", 43200) * 1000);
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT COUNT(*) FROM sell_items WHERE player = ? AND create_time < ?")) {

                statement.setString(1, player);
                statement.setLong(2, twelveHoursAgo);
                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                    return 0;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<List<SellItem>> getItems(MarketSortingType sortingType, Set<String> filters, int page, int pageSize) {
        return CompletableFuture.supplyAsync(() -> {
            List<SellItem> items = new ArrayList<>();
            try (Connection connection = dataSource.getConnection()) {
                String sql = buildQuery(sortingType, filters) + " LIMIT ? OFFSET ?";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    int paramIndex = 1;

                    if (filters != null && !filters.isEmpty()) {
                        for (String filter : filters) {
                            statement.setString(paramIndex++, "%" + filter + "%");
                        }
                    }

                    statement.setInt(paramIndex++, pageSize);
                    statement.setInt(paramIndex, (page - 1) * pageSize);

                    try (ResultSet rs = statement.executeQuery()) {
                        while (rs.next()) {
                            items.add(extractSellItemFromResultSet(rs));
                        }
                        return items;
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Integer> getItemsCount(MarketSortingType sortingType, Set<String> filters) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection()) {
                String countSql = buildCountQuery(filters);
                try (PreparedStatement statement = connection.prepareStatement(countSql)) {
                    if (filters != null && !filters.isEmpty()) {
                        int paramIndex = 1;
                        for (String filter : filters) {
                            statement.setString(paramIndex++, "%" + filter + "%");
                        }
                    }
                    try (ResultSet rs = statement.executeQuery()) {
                        if (rs.next()) {
                            return rs.getInt(1);
                        }
                        return 0;
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<List<SellItem>> getItems(MarketSortingType sortingType,
                                                      Set<String> additionalFilters,
                                                      CategoryType categoryFilter,
                                                      int page, int pageSize) {
        return CompletableFuture.supplyAsync(() -> {
            List<SellItem> items = new ArrayList<>();
            try (Connection connection = dataSource.getConnection()) {
                String sql = buildQuery(sortingType, additionalFilters, categoryFilter) + " LIMIT ? OFFSET ?";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    int paramIndex = 1;

                    if (categoryFilter != CategoryType.ALL) {
                        for (String tag : categoryFilter.getTags()) {
                            statement.setString(paramIndex++, "%" + tag + "%");
                        }
                    }

                    if (additionalFilters != null && !additionalFilters.isEmpty()) {
                        for (String filter : additionalFilters) {
                            statement.setString(paramIndex++, "%" + filter + "%");
                        }
                    }

                    statement.setInt(paramIndex++, pageSize);
                    statement.setInt(paramIndex, (page - 1) * pageSize);

                    try (ResultSet rs = statement.executeQuery()) {
                        while (rs.next()) {
                            items.add(extractSellItemFromResultSet(rs));
                        }
                        return items;
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Integer> getItemsCount(MarketSortingType sortingType,
                                                    Set<String> additionalFilters,
                                                    CategoryType categoryFilter) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection()) {
                String countSql = buildCountQuery(additionalFilters, categoryFilter);
                try (PreparedStatement statement = connection.prepareStatement(countSql)) {
                    int paramIndex = 1;

                    if (categoryFilter != CategoryType.ALL) {
                        for (String tag : categoryFilter.getTags()) {
                            statement.setString(paramIndex++, "%" + tag + "%");
                        }
                    }

                    if (additionalFilters != null && !additionalFilters.isEmpty()) {
                        for (String filter : additionalFilters) {
                            statement.setString(paramIndex++, "%" + filter + "%");
                        }
                    }

                    try (ResultSet rs = statement.executeQuery()) {
                        if (rs.next()) {
                            return rs.getInt(1);
                        }
                        return 0;
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<List<SellItem>> getItems(String owner,
                                                      MarketSortingType sortingType,
                                                      Set<String> additionalFilters,
                                                      CategoryType categoryFilter,
                                                      int page, int pageSize) {
        if(owner == null){
            return getItems(sortingType, additionalFilters, categoryFilter, page, pageSize);
        }
        return CompletableFuture.supplyAsync(() -> {
            List<SellItem> items = new ArrayList<>();
            try (Connection connection = dataSource.getConnection()) {
                String sql = buildOwnerQuery(owner, sortingType, additionalFilters, categoryFilter) + " LIMIT ? OFFSET ?";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    int paramIndex = 1;

                    statement.setString(paramIndex++, owner);

                    if (categoryFilter != CategoryType.ALL) {
                        for (String tag : categoryFilter.getTags()) {
                            statement.setString(paramIndex++, "%" + tag + "%");
                        }
                    }

                    if (additionalFilters != null && !additionalFilters.isEmpty()) {
                        for (String filter : additionalFilters) {
                            statement.setString(paramIndex++, "%" + filter + "%");
                        }
                    }

                    statement.setInt(paramIndex++, pageSize);
                    statement.setInt(paramIndex, (page - 1) * pageSize);

                    try (ResultSet rs = statement.executeQuery()) {
                        while (rs.next()) {
                            items.add(extractSellItemFromResultSet(rs));
                        }
                        return items;
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Integer> getItemsCount(String owner,
                                                    MarketSortingType sortingType,
                                                    Set<String> additionalFilters,
                                                    CategoryType categoryFilter) {
        if(owner == null){
            return getItemsCount(sortingType, additionalFilters, categoryFilter);
        }
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection()) {
                String countSql = buildOwnerCountQuery(owner, additionalFilters, categoryFilter);
                try (PreparedStatement statement = connection.prepareStatement(countSql)) {
                    int paramIndex = 1;

                    statement.setString(paramIndex++, owner);

                    if (categoryFilter != CategoryType.ALL) {
                        for (String tag : categoryFilter.getTags()) {
                            statement.setString(paramIndex++, "%" + tag + "%");
                        }
                    }

                    if (additionalFilters != null && !additionalFilters.isEmpty()) {
                        for (String filter : additionalFilters) {
                            statement.setString(paramIndex++, "%" + filter + "%");
                        }
                    }

                    try (ResultSet rs = statement.executeQuery()) {
                        if (rs.next()) {
                            return rs.getInt(1);
                        }
                        return 0;
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private String buildQuery(MarketSortingType sortingType,
                              Set<String> additionalFilters,
                              CategoryType categoryFilter) {
        StringBuilder sql = new StringBuilder("SELECT * FROM sell_items");
        List<String> conditions = new ArrayList<>();

        if (categoryFilter != CategoryType.ALL) {
            StringBuilder categoryCondition = new StringBuilder("(");
            List<String> tagConditions = new ArrayList<>();

            for (String tag : categoryFilter.getTags()) {
                tagConditions.add("tags LIKE ?");
            }

            categoryCondition.append(String.join(" OR ", tagConditions));
            categoryCondition.append(")");
            conditions.add(categoryCondition.toString());
        }

        if (additionalFilters != null && !additionalFilters.isEmpty()) {
            StringBuilder filtersCondition = new StringBuilder("(");
            List<String> filterConditions = new ArrayList<>();

            for (String filter : additionalFilters) {
                filterConditions.add("tags LIKE ?");
            }

            filtersCondition.append(String.join(" AND ", filterConditions));
            filtersCondition.append(")");
            conditions.add(filtersCondition.toString());
        }

        if (!conditions.isEmpty()) {
            sql.append(" WHERE ");
            sql.append(String.join(" AND ", conditions));
        }

        sql.append(getOrderByClause(sortingType));

        return sql.toString();
    }

    private String buildOwnerQuery(String owner,
                                   MarketSortingType sortingType,
                                   Set<String> additionalFilters,
                                   CategoryType categoryFilter) {
        StringBuilder sql = new StringBuilder("SELECT * FROM sell_items WHERE player = ?");

        if (categoryFilter != CategoryType.ALL) {
            sql.append(" AND (");
            for (int i = 0; i < categoryFilter.getTags().size(); i++) {
                if (i > 0) {
                    sql.append(" OR ");
                }
                sql.append("tags LIKE ?");
            }
            sql.append(")");
        }

        if (additionalFilters != null && !additionalFilters.isEmpty()) {
            sql.append(" AND (");
            for (int i = 0; i < additionalFilters.size(); i++) {
                if (i > 0) {
                    sql.append(" AND ");
                }
                sql.append("tags LIKE ?");
            }
            sql.append(")");
        }

        sql.append(getOrderByClause(sortingType));

        return sql.toString();
    }

    private String buildCountQuery(Set<String> filters) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM sell_items");

        if (filters != null && !filters.isEmpty()) {
            sql.append(" WHERE ");
            for (int i = 0; i < filters.size(); i++) {
                if (i > 0) {
                    sql.append(" AND ");
                }
                sql.append("tags LIKE ?");
            }
        }

        return sql.toString();
    }

    private String buildCountQuery(Set<String> additionalFilters, CategoryType categoryFilter) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM sell_items");
        List<String> conditions = new ArrayList<>();

        if (categoryFilter != CategoryType.ALL) {
            StringBuilder categoryCondition = new StringBuilder("(");
            List<String> tagConditions = new ArrayList<>();

            for (String tag : categoryFilter.getTags()) {
                tagConditions.add("tags LIKE ?");
            }

            categoryCondition.append(String.join(" OR ", tagConditions));
            categoryCondition.append(")");
            conditions.add(categoryCondition.toString());
        }

        if (additionalFilters != null && !additionalFilters.isEmpty()) {
            StringBuilder filtersCondition = new StringBuilder("(");
            List<String> filterConditions = new ArrayList<>();

            for (String filter : additionalFilters) {
                filterConditions.add("tags LIKE ?");
            }

            filtersCondition.append(String.join(" AND ", filterConditions));
            filtersCondition.append(")");
            conditions.add(filtersCondition.toString());
        }

        if (!conditions.isEmpty()) {
            sql.append(" WHERE ");
            sql.append(String.join(" AND ", conditions));
        }

        return sql.toString();
    }

    private String buildOwnerCountQuery(String owner, Set<String> additionalFilters, CategoryType categoryFilter) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM sell_items WHERE player = ?");

        if (categoryFilter != CategoryType.ALL) {
            sql.append(" AND (");
            for (int i = 0; i < categoryFilter.getTags().size(); i++) {
                if (i > 0) {
                    sql.append(" OR ");
                }
                sql.append("tags LIKE ?");
            }
            sql.append(")");
        }

        if (additionalFilters != null && !additionalFilters.isEmpty()) {
            sql.append(" AND (");
            for (int i = 0; i < additionalFilters.size(); i++) {
                if (i > 0) {
                    sql.append(" AND ");
                }
                sql.append("tags LIKE ?");
            }
            sql.append(")");
        }

        return sql.toString();
    }

    private String getOrderByClause(MarketSortingType sortingType) {
        return " ORDER BY " + switch (sortingType) {
            case CHEAPEST_FIRST -> "(price * amount) ASC";
            case EXPENSIVE_FIRST -> "(price * amount) DESC";
            case CHEAPEST_PER_UNIT -> "price ASC";
            case EXPENSIVE_PER_UNIT -> "price DESC";
            case NEWEST_FIRST -> "create_time DESC";
            case OLDEST_FIRST -> "create_time ASC";
            default -> "id ASC";
        };
    }

    public CompletableFuture<Void> updateItem(SellItem item) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "UPDATE sell_items SET player = ?, itemstack = ?, tags = ?, " +
                                 "price = ?, amount = ?, by_one = ?, create_time = ? WHERE id = ?")) {

                statement.setString(1, item.getPlayer());
                statement.setString(2, item.getItemStack());
                statement.setString(3, String.join(",", item.getTags()));
                statement.setInt(4, item.getPrice());
                statement.setInt(5, item.getAmount());
                statement.setBoolean(6, item.isByOne());
                statement.setLong(7, item.getCreateTime());
                statement.setInt(8, item.getId());

                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Optional<SellItem>> getItem(int id) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT * FROM sell_items WHERE id = ?")) {

                statement.setInt(1, id);

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(extractSellItemFromResultSet(rs));
                    }
                    return Optional.empty();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Void> deleteItem(int id) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "DELETE FROM sell_items WHERE id = ?")) {

                statement.setInt(1, id);
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Void> moveExpiredItems() {
        return CompletableFuture.runAsync(() -> {
            long twelveHoursAgo = System.currentTimeMillis() - (ConfigManager.getLong("settings/settings.yml", "lifetime.sell", 43200) * 1000);
            try (Connection connection = dataSource.getConnection()) {
                connection.setAutoCommit(false);
                try (PreparedStatement selectStatement = connection.prepareStatement(
                        "SELECT * FROM sell_items WHERE create_time < ?");
                     PreparedStatement insertStatement = connection.prepareStatement(
                             "INSERT INTO unsold_items (player, itemstack, tags, amount, price, by_one, create_time) " +
                                     "VALUES (?, ?, ?, ?, ?, ?, ?)");
                     PreparedStatement deleteStatement = connection.prepareStatement(
                             "DELETE FROM sell_items WHERE id = ?")) {

                    selectStatement.setLong(1, twelveHoursAgo);
                    try (ResultSet rs = selectStatement.executeQuery()) {
                        while (rs.next()) {
                            SellItem item = extractSellItemFromResultSet(rs);
                            ItemStack itemStack = item.decodeItemStack();
                            LiteAuction.getInstance().getCommunicationManager().publishMessage("update", "market " + item.getId());
                            LiteAuction.getInstance().getCommunicationManager().publishMessage("hover", item.getPlayer() + " " + ItemHoverUtil.getHoverItemMessage(Parser.color("&#00D4FB▶ &#9AF5FB%item%&f &#9AF5FBx" + item.getAmount() + " &fоказался слишком дорогой или никому не нужен. Заберите предмет с Аукциона!"), itemStack));

                            insertStatement.setString(1, item.getPlayer());
                            insertStatement.setString(2, item.getItemStack());
                            insertStatement.setString(3, String.join(",", item.getTags()));
                            insertStatement.setInt(4, item.getAmount());
                            insertStatement.setInt(5, item.getPrice());
                            insertStatement.setBoolean(6, item.isByOne());
                            insertStatement.setLong(7, System.currentTimeMillis());
                            insertStatement.addBatch();

                            deleteStatement.setInt(1, item.getId());
                            deleteStatement.addBatch();
                        }

                        insertStatement.executeBatch();
                        deleteStatement.executeBatch();
                        connection.commit();
                    }
                } catch (SQLException e) {
                    connection.rollback();
                    throw e;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private String buildQuery(MarketSortingType sortingType, Set<String> filters) {
        StringBuilder sql = new StringBuilder("SELECT * FROM sell_items");

        if (filters != null && !filters.isEmpty()) {
            sql.append(" WHERE ");
            for (int i = 0; i < filters.size(); i++) {
                if (i > 0) {
                    sql.append(" AND ");
                }
                sql.append("tags LIKE ?");
            }
        }

        sql.append(getOrderByClause(sortingType));

        return sql.toString();
    }

    private SellItem extractSellItemFromResultSet(ResultSet rs) throws SQLException {
        Set<String> tags = new HashSet<>();
        String tagsStr = rs.getString("tags");
        if (tagsStr != null && !tagsStr.isEmpty()) {
            tags.addAll(Arrays.asList(tagsStr.split(",")));
        }

        return new SellItem(
                rs.getInt("id"),
                rs.getString("player"),
                rs.getString("itemstack"),
                tags,
                rs.getInt("price"),
                rs.getInt("amount"),
                rs.getBoolean("by_one"),
                rs.getLong("create_time"),
                false
        );
    }
}