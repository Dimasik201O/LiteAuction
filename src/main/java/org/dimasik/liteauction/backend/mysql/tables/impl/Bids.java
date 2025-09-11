package org.dimasik.liteauction.backend.mysql.tables.impl;

import com.zaxxer.hikari.HikariDataSource;
import org.dimasik.liteauction.backend.mysql.tables.AbstractTable;
import org.dimasik.liteauction.backend.mysql.models.Bid;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class Bids extends AbstractTable {
    public Bids(HikariDataSource dataSource) {
        super(dataSource);
    }

    public CompletableFuture<Void> createTable() {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 Statement statement = connection.createStatement()) {
                String sql = "CREATE TABLE IF NOT EXISTS bids (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "item_id INT NOT NULL, " +
                        "player VARCHAR(16) NOT NULL, " +
                        "price INT NOT NULL, " +
                        "FOREIGN KEY (item_id) REFERENCES bid_items(id) ON DELETE CASCADE)";
                statement.execute(sql);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Integer> addBid(int itemId, String player, int price) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "INSERT INTO bids (item_id, player, price) VALUES (?, ?, ?)",
                         Statement.RETURN_GENERATED_KEYS)) {

                statement.setInt(1, itemId);
                statement.setString(2, player);
                statement.setInt(3, price);

                int affectedRows = statement.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Creating bid failed, no rows affected.");
                }

                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                    throw new SQLException("Creating bid failed, no ID obtained.");
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Optional<Bid>> getBidById(int id) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT * FROM bids WHERE id = ?")) {

                statement.setInt(1, id);
                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(extractBidFromResultSet(rs));
                    }
                    return Optional.empty();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<List<Bid>> getBidsByItemId(int itemId) {
        return CompletableFuture.supplyAsync(() -> {
            List<Bid> bids = new ArrayList<>();
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT * FROM bids WHERE item_id = ? ORDER BY price ASC")) {
                statement.setInt(1, itemId);
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        bids.add(extractBidFromResultSet(rs));
                    }
                    return bids;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<List<Bid>> getPlayerBids(String player) {
        return CompletableFuture.supplyAsync(() -> {
            List<Bid> bids = new ArrayList<>();
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT * FROM bids WHERE player = ? ORDER BY id DESC")) {
                statement.setString(1, player);
                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        bids.add(extractBidFromResultSet(rs));
                    }
                    return bids;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Optional<Bid>> getHighestBidForItem(int itemId) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT * FROM bids WHERE item_id = ? ORDER BY price DESC LIMIT 1")) {

                statement.setInt(1, itemId);
                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(extractBidFromResultSet(rs));
                    }
                    return Optional.empty();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Integer> getBidCountForItem(int itemId) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT COUNT(*) FROM bids WHERE item_id = ?")) {

                statement.setInt(1, itemId);
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

    public CompletableFuture<Void> updateBid(Bid bid) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "UPDATE bids SET item_id = ?, player = ?, price = ? WHERE id = ?")) {

                statement.setInt(1, bid.getItemId());
                statement.setString(2, bid.getPlayer());
                statement.setInt(3, bid.getPrice());
                statement.setInt(4, bid.getId());

                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Void> deleteBid(int id) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "DELETE FROM bids WHERE id = ?")) {

                statement.setInt(1, id);
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Void> deleteBidsByItemId(int itemId) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "DELETE FROM bids WHERE item_id = ?")) {

                statement.setInt(1, itemId);
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private Bid extractBidFromResultSet(ResultSet rs) throws SQLException {
        return new Bid(
                rs.getInt("id"),
                rs.getInt("item_id"),
                rs.getString("player"),
                rs.getInt("price")
        );
    }
}