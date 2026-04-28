package com.auction.server.dao;
import com.auction.server.models.Item;
import com.auction.server.models.AuctionStatus;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemDAO {

    /**
     * Cập nhật trạng thái các sản phẩm từ PENDING sang RUNNING khi start_time đã qua.
     * @return Danh sách các Item vừa được cập nhật.
     */
    public List<Item> updatePendingItemsToRunning() {
        List<Item> updatedItems = new ArrayList<>();
        String findSql = "SELECT * FROM items WHERE status = 'PENDING' AND start_time <= NOW()";
        String updateSql = "UPDATE items SET status = 'RUNNING' WHERE id = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try {
            conn.setAutoCommit(false); // Bắt đầu giao dịch
            try (PreparedStatement findStmt = conn.prepareStatement(findSql);
                 ResultSet rs = findStmt.executeQuery()) {
                while (rs.next()) {
                    Item item = mapRowToItem(rs);
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setInt(1, item.getId());
                        if (updateStmt.executeUpdate() > 0) {
                            updatedItems.add(item);
                        }
                    }
                }
            }
            conn.commit(); // Hoàn tất giao dịch
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return updatedItems;
    }

    /**
     * Cập nhật trạng thái các sản phẩm từ RUNNING sang ENDED khi end_time đã qua.
     * @return Danh sách các Item vừa được cập nhật.
     */
    public List<Item> updateRunningItemsToEnded() {
        List<Item> updatedItems = new ArrayList<>();
        String findSql = "SELECT * FROM items WHERE status = 'RUNNING' AND end_time <= NOW()";
        String updateSql = "UPDATE items SET status = 'ENDED' WHERE id = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try {
            conn.setAutoCommit(false); // Bắt đầu giao dịch
            try (PreparedStatement findStmt = conn.prepareStatement(findSql);
                 ResultSet rs = findStmt.executeQuery()) {
                while (rs.next()) {
                    Item item = mapRowToItem(rs);
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setInt(1, item.getId());
                        if (updateStmt.executeUpdate() > 0) {
                            updatedItems.add(item);
                        }
                    }
                }
            }
            conn.commit(); // Hoàn tất giao dịch
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return updatedItems;
    }

    public boolean addItem(Item item) {
        String sql = "INSERT INTO items (seller_id, name, description, starting_price, current_max_price, status, category, start_time, end_time) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, item.getSellerId());
            pstmt.setString(2, item.getName());
            pstmt.setString(3, item.getDescription());
            pstmt.setDouble(4, item.getStartingPrice());
            pstmt.setDouble(5, item.getStartingPrice());
            pstmt.setString(6, item.getStatus().name());
            pstmt.setString(7, item.getCategory());
            pstmt.setTimestamp(8, Timestamp.valueOf(item.getStartTime()));
            pstmt.setTimestamp(9, Timestamp.valueOf(item.getEndTime()));

            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        item.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public boolean updateItem(Item item, int sellerId) {
        String sql = "UPDATE items SET name = ?, description = ?, starting_price = ?, category = ?, start_time = ?, end_time = ? " +
                     "WHERE id = ? AND seller_id = ? AND status = 'PENDING'";
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, item.getName());
            pstmt.setString(2, item.getDescription());
            pstmt.setDouble(3, item.getStartingPrice());
            pstmt.setString(4, item.getCategory());
            pstmt.setTimestamp(5, Timestamp.valueOf(item.getStartTime()));
            pstmt.setTimestamp(6, Timestamp.valueOf(item.getEndTime()));
            pstmt.setInt(7, item.getId());
            pstmt.setInt(8, sellerId);

            int rowsUpdated = pstmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Item getItemById(int itemId) {
        String sql = "SELECT * FROM items WHERE id = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToItem(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    private Item mapRowToItem(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int sellerId = rs.getInt("seller_id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        double startingPrice = rs.getDouble("starting_price");
        double currentMaxPrice = rs.getDouble("current_max_price");
        java.time.LocalDateTime startTime = rs.getTimestamp("start_time").toLocalDateTime();
        java.time.LocalDateTime endTime = rs.getTimestamp("end_time").toLocalDateTime();
        String statusStr = rs.getString("status");
        AuctionStatus status = AuctionStatus.valueOf(statusStr);
        int highestBidderId = rs.getInt("highest_bidder_id");
        String category = rs.getString("category");

        Item item = new Item(id, sellerId, name, description, startingPrice, startTime, endTime, category) {
            @Override
            public void printInfo() {
                System.out.println("Item: " + name);
            }
        };

        item.setHighestBidderId(highestBidderId);
        item.setCurrentMaxPrice(currentMaxPrice);
        item.setStatus(status);
        return item;
    }

    public List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT DISTINCT category FROM items WHERE category IS NOT NULL AND category != '' ORDER BY category";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                categories.add(rs.getString("category"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    public List<Item> getItemsByCategory(String category, int page, int limit) {
        List<Item> items = new ArrayList<>();
        String sql = "SELECT * FROM items WHERE category = ? ORDER BY id DESC LIMIT ? OFFSET ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, category);
            pstmt.setInt(2, limit);
            pstmt.setInt(3, (page - 1) * limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapRowToItem(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public int getCategoryItemCount(String category) {
        String sql = "SELECT COUNT(*) FROM items WHERE category = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, category);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean updateStatus(int id, AuctionStatus newStatus){
        String sql = "UPDATE items SET status = ? WHERE id = ?";
        Connection conn  = DatabaseConnection.getInstance().getConnection();
        try(PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(2,id);
            pstmt.setString(1, newStatus.name());
            int rowsUpdated = pstmt.executeUpdate();
            return rowsUpdated > 0;
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateCurrentMaxPrice(int itemId, double newPrice, int bidderId){
        String sql = "UPDATE items SET current_max_price = ?, highest_bidder_id = ? WHERE id = ?"+
                        "AND current_max_price < ? AND status = 'RUNNING'";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try(PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setDouble(1, newPrice);
            pstmt.setInt(2, bidderId);
            pstmt.setInt(3, itemId);
            pstmt.setDouble(4, newPrice);
            int rowsUpdated = pstmt.executeUpdate();
            return rowsUpdated > 0;
        }catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public List<Item> getAllItems(int page, int limit) {
        List<Item> itemList = new ArrayList<>();
        String sql = "SELECT * FROM items ORDER BY id DESC LIMIT ? OFFSET ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            pstmt.setInt(2, (page - 1) * limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    itemList.add(mapRowToItem(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return itemList;
    }

    public int getTotalItemCount() {
        String sql = "SELECT COUNT(*) FROM items";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<Item> searchItemsByName(String keyword, int page, int limit) {
        List<Item> itemList = new ArrayList<>();
        String sql = "SELECT * FROM items WHERE name LIKE ? ORDER BY id DESC LIMIT ? OFFSET ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + keyword + "%");
            pstmt.setInt(2, limit);
            pstmt.setInt(3, (page - 1) * limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    itemList.add(mapRowToItem(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return itemList;
    }

    public int getSearchItemCount(String keyword) {
        String sql = "SELECT COUNT(*) FROM items WHERE name LIKE ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + keyword + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<Item> getWonItemsByUserId(int userId) {
        List<Item> wonItems = new ArrayList<>();
        String sql = "SELECT * FROM items WHERE highest_bidder_id = ? AND status = 'ENDED' ORDER BY end_time DESC";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    wonItems.add(mapRowToItem(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("✗ [Lỗi lấy sản phẩm đã thắng] " + e.getMessage());
        }
        return wonItems;
    }

    public List<Item> getItemsBySellerId(int sellerId) {
        List<Item> items = new ArrayList<>();
        String sql = "SELECT * FROM items WHERE seller_id = ? ORDER BY id DESC";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, sellerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapRowToItem(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("✗ [Lỗi lấy sản phẩm theo seller] " + e.getMessage());
        }
        return items;
    }

    public boolean deleteItem(int itemId) {
        String deleteBidsSql = "DELETE FROM bids WHERE item_id = ?";
        String deleteItemSql = "DELETE FROM items WHERE id = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmtBids = conn.prepareStatement(deleteBidsSql)) {
                pstmtBids.setInt(1, itemId);
                pstmtBids.executeUpdate();
            }
            try (PreparedStatement pstmtItem = conn.prepareStatement(deleteItemSql)) {
                pstmtItem.setInt(1, itemId);
                int rowsDeleted = pstmtItem.executeUpdate();
                if (rowsDeleted > 0) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            }
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}