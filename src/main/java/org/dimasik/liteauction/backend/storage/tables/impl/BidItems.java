package org.dimasik.liteauction.backend.storage.tables.impl;

import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.inventory.ItemStack;
import org.dimasik.liteauction.LiteAuction;
import org.dimasik.liteauction.backend.config.ConfigManager;
import org.dimasik.liteauction.backend.enums.BidsSortingType;
import org.dimasik.liteauction.backend.enums.CategoryType;
import org.dimasik.liteauction.backend.storage.tables.AbstractTable;
import org.dimasik.liteauction.backend.storage.models.Bid;
import org.dimasik.liteauction.backend.storage.models.BidItem;
import org.dimasik.liteauction.backend.utils.Formatter;
import org.dimasik.liteauction.backend.utils.ItemHoverUtil;
import org.dimasik.liteauction.backend.utils.Parser;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class BidItems extends AbstractTable {
    public BidItems(HikariDataSource dataSource) {
        super(dataSource);
    }

    public CompletableFuture<Void> createTable() {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement()) {
                String sql = "CREATE TABLE IF NOT EXISTS bid_items (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "player VARCHAR(16) NOT NULL, " +
                        "itemstack TEXT NOT NULL, " +
                        "tags TEXT, " +
                        "start_price INT NOT NULL, " +
                        "current_price INT NOT NULL, " +
                        "step INT NOT NULL, " +
                        "expiry_time BIGINT NOT NULL, " +
                        "create_time BIGINT NOT NULL)";

                sql = LiteAuction.getInstance().getDatabaseManager().editQuery(sql);

                statement.execute(sql);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Integer> addItem(String player, String itemStack, Set<String> tags,
                                                 int startPrice, int step, long expiryTime) {
        return CompletableFuture.supplyAsync(() -> {
            long createTime = System.currentTimeMillis();
            int currentPrice = startPrice;

            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "INSERT INTO bid_items (player, itemstack, tags, start_price, current_price, step, expiry_time, create_time) " +
                                 "VALUES (?, ?, ?, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {

                statement.setString(1, player);
                statement.setString(2, itemStack);
                statement.setString(3, String.join(",", tags));
                statement.setInt(4, startPrice);
                statement.setInt(5, currentPrice);
                statement.setInt(6, step);
                statement.setLong(7, expiryTime);
                statement.setLong(8, createTime);

                int affectedRows = statement.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Creating bid item failed, no rows affected.");
                }

                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                    throw new SQLException("Creating bid item failed, no ID obtained.");
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Void> updateCurrentPrice(int itemId, int newPrice) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "UPDATE bid_items SET current_price = ? WHERE id = ?")) {

                statement.setInt(1, newPrice);
                statement.setInt(2, itemId);
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Optional<Integer>> getCurrentPrice(int itemId) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT current_price FROM bid_items WHERE id = ?")) {

                statement.setInt(1, itemId);
                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(rs.getInt("current_price"));
                    }
                    return Optional.empty();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Optional<BidItem>> getItem(int id) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT * FROM bid_items WHERE id = ?")) {

                statement.setInt(1, id);
                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(extractBidItemFromResultSet(rs));
                    }
                    return Optional.empty();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<List<BidItem>> getAllItems() {
        return CompletableFuture.supplyAsync(() -> {
            List<BidItem> items = new ArrayList<>();
            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery("SELECT * FROM bid_items")) {
                while (rs.next()) {
                    items.add(extractBidItemFromResultSet(rs));
                }
                return items;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<List<BidItem>> getActiveItems() {
        return CompletableFuture.supplyAsync(() -> {
            List<BidItem> items = new ArrayList<>();
            long currentTime = System.currentTimeMillis();
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT * FROM bid_items WHERE expiry_time > ?")) {

                statement.setLong(1, currentTime);
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        items.add(extractBidItemFromResultSet(rs));
                    }
                    return items;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Void> moveExpiredItems(){
        return CompletableFuture.runAsync(() -> {
            try {
                List<BidItem> items = getExpiredItems().get();
                for(BidItem bidItem : items){
                    ItemStack itemStack = bidItem.decodeItemStack();
                    List<Bid> bids = LiteAuction.getInstance().getDatabaseManager().getBidsManager().getBidsByItemId(bidItem.getId()).get();
                    if(bids.isEmpty()){
                        LiteAuction.getInstance().getCommunicationManager().publishMessage("hover", bidItem.getPlayer() + " " + ItemHoverUtil.getHoverItemMessage(Parser.color("&#00D4FB▶ &#9AF5FB%item%&f &#9AF5FBx" + itemStack.getAmount() + " &fоказался слишком дорогой или никому не нужен. Заберите предмет с Аукциона!"), itemStack));
                        LiteAuction.getInstance().getDatabaseManager().getUnsoldItemsManager().addItem(
                                bidItem.getPlayer(),
                                bidItem.getItemStack(),
                                bidItem.getTags(),
                                (bidItem.getCurrentPrice() / itemStack.getAmount()) * itemStack.getAmount(),
                                itemStack.getAmount(),
                                true
                        );
                    }
                    else {
                        Bid lastBid = bids.get(bids.size() - 1);
                        LiteAuction.getEconomyEditor().addBalance(lastBid.getPlayer(), lastBid.getPrice());
                        LiteAuction.getInstance().getDatabaseManager().getUnsoldItemsManager().addItem(
                                lastBid.getPlayer(),
                                bidItem.getItemStack(),
                                bidItem.getTags(),
                                (bidItem.getCurrentPrice() / itemStack.getAmount()) * itemStack.getAmount(),
                                itemStack.getAmount(),
                                true
                        );
                        LiteAuction.getInstance().getCommunicationManager().publishMessage("msg", lastBid.getPlayer() + " " + Parser.color("&#00D4FB▶ &fВы выкупили лот игрока &6" + bidItem.getPlayer() + " &fза &#FEC800" + Formatter.formatPrice(lastBid.getPrice()) + "&f. Заберите его из меню просроченных предметов."));
                        LiteAuction.getInstance().getCommunicationManager().publishMessage("msg", bidItem.getPlayer() + " " + Parser.color("&#00D4FB▶ &fВаш лот был продан игроку &#00D4FB" + lastBid.getPlayer() + " &fза &#FEC800" + Formatter.formatPrice(lastBid.getPrice())));                    }
                    LiteAuction.getInstance().getCommunicationManager().publishMessage("update", "bids " + bidItem.getId() + " delete");
                }
                deleteExpiredItems();
            }
            catch (InterruptedException | ExecutionException e){
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<List<BidItem>> getExpiredItems() {
        return CompletableFuture.supplyAsync(() -> {
            List<BidItem> items = new ArrayList<>();
            long currentTime = System.currentTimeMillis();
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT * FROM bid_items WHERE expiry_time <= ?")) {

                statement.setLong(1, currentTime);
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        items.add(extractBidItemFromResultSet(rs));
                    }
                    return items;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<List<BidItem>> getPlayerItems(String player) {
        return CompletableFuture.supplyAsync(() -> {
            List<BidItem> items = new ArrayList<>();
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT * FROM bid_items WHERE player = ? ORDER BY create_time DESC")) {
                statement.setString(1, player);
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        items.add(extractBidItemFromResultSet(rs));
                    }
                    return items;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<List<BidItem>> getItems(BidsSortingType sortingType,
                                                     Set<String> additionalFilters,
                                                     CategoryType categoryFilter) {
        return CompletableFuture.supplyAsync(() -> {
            List<BidItem> items = new ArrayList<>();
            try (Connection connection = dataSource.getConnection()) {
                String sql = buildQuery(sortingType, additionalFilters, categoryFilter);
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

                    try (ResultSet rs = statement.executeQuery()) {
                        while (rs.next()) {
                            items.add(extractBidItemFromResultSet(rs));
                        }
                        return items;
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<List<BidItem>> getItems(String owner,
                                                     BidsSortingType sortingType,
                                                     Set<String> additionalFilters,
                                                     CategoryType categoryFilter) {
        if (owner == null) {
            return getItems(sortingType, additionalFilters, categoryFilter);
        }
        return CompletableFuture.supplyAsync(() -> {
            List<BidItem> items = new ArrayList<>();
            try (Connection connection = dataSource.getConnection()) {
                String sql = buildOwnerQuery(owner, sortingType, additionalFilters, categoryFilter);
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

                    try (ResultSet rs = statement.executeQuery()) {
                        while (rs.next()) {
                            items.add(extractBidItemFromResultSet(rs));
                        }
                        return items;
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private String buildQuery(BidsSortingType sortingType,
                              Set<String> additionalFilters,
                              CategoryType categoryFilter) {
        StringBuilder sql = new StringBuilder("SELECT * FROM bid_items");
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
                                   BidsSortingType sortingType,
                                   Set<String> additionalFilters,
                                   CategoryType categoryFilter) {
        StringBuilder sql = new StringBuilder("SELECT * FROM bid_items WHERE player = ? AND expiry_time > " + System.currentTimeMillis());

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

    private String getOrderByClause(BidsSortingType sortingType) {
        return " ORDER BY " + switch (sortingType) {
            case CHEAPEST_FIRST -> "current_price ASC";
            case EXPENSIVE_FIRST -> "current_price DESC";
            case NEWEST_FIRST -> "create_time DESC";
            case OLDEST_FIRST -> "create_time ASC";
            default -> "id ASC";
        };
    }

    public CompletableFuture<Void> updateItem(BidItem item) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "UPDATE bid_items SET player = ?, itemstack = ?, tags = ?, " +
                                 "start_price = ?, current_price = ?, step = ?, expiry_time = ?, create_time = ? WHERE id = ?")) {

                statement.setString(1, item.getPlayer());
                statement.setString(2, item.getItemStack());
                statement.setString(3, String.join(",", item.getTags()));
                statement.setInt(4, item.getStartPrice());
                statement.setInt(5, item.getCurrentPrice());
                statement.setInt(6, item.getStep());
                statement.setLong(7, item.getExpiryTime());
                statement.setLong(8, item.getCreateTime());
                statement.setInt(9, item.getId());

                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Void> deleteItem(int id) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "DELETE FROM bid_items WHERE id = ?")) {

                statement.setInt(1, id);
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Void> deleteExpiredItems() {
        return CompletableFuture.runAsync(() -> {
            long currentTime = System.currentTimeMillis();
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "DELETE FROM bid_items WHERE expiry_time <= ?")) {

                statement.setLong(1, currentTime);
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private BidItem extractBidItemFromResultSet(ResultSet rs) throws SQLException {
        Set<String> tags = new HashSet<>();
        String tagsStr = rs.getString("tags");
        if (tagsStr != null && !tagsStr.isEmpty()) {
            tags.addAll(Arrays.asList(tagsStr.split(",")));
        }

        return new BidItem(
                rs.getInt("id"),
                rs.getString("player"),
                rs.getString("itemstack"),
                tags,
                rs.getInt("start_price"),
                rs.getInt("current_price"),
                rs.getInt("step"),
                rs.getLong("expiry_time"),
                rs.getLong("create_time")
        );
    }
}