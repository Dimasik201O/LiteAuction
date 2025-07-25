package org.dimasik.liteauction.backend.mysql.impl;

import com.zaxxer.hikari.HikariDataSource;
import org.dimasik.liteauction.backend.mysql.models.UnsoldItem;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class UnsoldItems {
    private final HikariDataSource dataSource;

    public UnsoldItems(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public CompletableFuture<Void> createTable() {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement()) {
                String sql = "CREATE TABLE IF NOT EXISTS unsold_items (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "player VARCHAR(16) NOT NULL, " +
                        "itemstack TEXT NOT NULL, " +
                        "tags TEXT, " +
                        "amount INT NOT NULL, " +
                        "price INT NOT NULL, " +
                        "create_time BIGINT NOT NULL)";
                statement.execute(sql);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<List<UnsoldItem>> getAllItems() {
        return CompletableFuture.supplyAsync(() -> {
            List<UnsoldItem> items = new ArrayList<>();
            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery("SELECT * FROM unsold_items")) {
                while (rs.next()) {
                    items.add(extractUnsoldItemFromResultSet(rs));
                }
                return items;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Optional<UnsoldItem>> getItemById(int id) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT * FROM unsold_items WHERE id = ?")) {

                statement.setInt(1, id);
                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(extractUnsoldItemFromResultSet(rs));
                    }
                    return Optional.empty();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<List<UnsoldItem>> getPlayerItems(String player) {
        return CompletableFuture.supplyAsync(() -> {
            List<UnsoldItem> items = new ArrayList<>();
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT * FROM unsold_items WHERE player = ?")) {

                statement.setString(1, player);
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        items.add(extractUnsoldItemFromResultSet(rs));
                    }
                    return items;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Integer> deleteExpiredItems() {
        return CompletableFuture.supplyAsync(() -> {
            long sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000);
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "DELETE FROM unsold_items WHERE create_time < ?")) {

                statement.setLong(1, sevenDaysAgo);
                return statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Void> updateItem(UnsoldItem item) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "UPDATE unsold_items SET player = ?, itemstack = ?, tags = ?, " +
                                 "amount = ?, price = ?, create_time = ? WHERE id = ?")) {

                statement.setString(1, item.getPlayer());
                statement.setString(2, item.getItemStack());
                statement.setString(3, String.join(",", item.getTags()));
                statement.setInt(4, item.getAmount());
                statement.setInt(5, item.getPrice());
                statement.setLong(6, item.getCreateTime());
                statement.setInt(7, item.getId());

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
                         "DELETE FROM unsold_items WHERE id = ?")) {

                statement.setInt(1, id);
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private UnsoldItem extractUnsoldItemFromResultSet(ResultSet rs) throws SQLException {
        Set<String> tags = new HashSet<>();
        String tagsStr = rs.getString("tags");
        if (tagsStr != null && !tagsStr.isEmpty()) {
            tags.addAll(Arrays.asList(tagsStr.split(",")));
        }

        return new UnsoldItem(
                rs.getInt("id"),
                rs.getString("player"),
                rs.getString("itemstack"),
                tags,
                rs.getInt("amount"),
                rs.getInt("price"),
                rs.getLong("create_time")
        );
    }
}