package com.auction.server.dao;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.auction.server.models.AuctionStatus;
import com.auction.server.models.BidTransaction;
import com.auction.server.models.BidTransactionDTO;
import com.auction.server.models.Bidder;
import com.auction.server.models.BidderDTO;
import com.auction.server.models.Item;

public class BidsDAO {
    // Thực hiện đặt giá mới
    public boolean executeBid(int itemId, int newBidderId, double newBidAmount) {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try {
            conn.setAutoCommit(false);

            String checkItemSql = "SELECT current_max_price, highest_bidder_id, status FROM items WHERE id = ? FOR UPDATE";
            double currentMaxPrice = 0;
            int oldBidderId = 0;
            
            try (PreparedStatement checkStmt = conn.prepareStatement(checkItemSql)) {
                checkStmt.setInt(1, itemId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        String status = rs.getString("status");
                        if (!"RUNNING".equals(status)) {
                            conn.rollback(); return false; 
                        }
                        currentMaxPrice = rs.getDouble("current_max_price");
                        oldBidderId = rs.getInt("highest_bidder_id"); 
                    } else {
                        conn.rollback(); return false; 
                    }
                }
            }

            if (newBidAmount <= currentMaxPrice) {
                conn.rollback(); return false; 
            }

            String deductSql = "UPDATE bidders SET account_balance = account_balance - ? WHERE user_id = ? AND account_balance >= ?";
            try (PreparedStatement deductStmt = conn.prepareStatement(deductSql)) {
                deductStmt.setDouble(1, newBidAmount);
                deductStmt.setInt(2, newBidderId);
                deductStmt.setDouble(3, newBidAmount);
                if (deductStmt.executeUpdate() == 0) {
                    conn.rollback(); return false; 
                }
            }

            if (oldBidderId != 0) {
                String refundSql = "UPDATE bidders SET account_balance = account_balance + ? WHERE user_id = ?";
                try (PreparedStatement refundStmt = conn.prepareStatement(refundSql)) {
                    refundStmt.setDouble(1, currentMaxPrice); // Trả lại đúng số tiền họ đã cược
                    refundStmt.setInt(2, oldBidderId);
                    refundStmt.executeUpdate();
                }
            }

            String updateItemSql = "UPDATE items SET current_max_price = ?, highest_bidder_id = ? WHERE id = ?";
            try (PreparedStatement updateItemStmt = conn.prepareStatement(updateItemSql)) {
                updateItemStmt.setDouble(1, newBidAmount);
                updateItemStmt.setInt(2, newBidderId);
                updateItemStmt.setInt(3, itemId);
                updateItemStmt.executeUpdate();
            }

            String insertHistorySql = "INSERT INTO bids (item_id, bidder_id, bid_amount) VALUES (?, ?, ?)";
            try (PreparedStatement insertHistoryStmt = conn.prepareStatement(insertHistorySql)) {
                insertHistoryStmt.setInt(1, itemId);
                insertHistoryStmt.setInt(2, newBidderId);
                insertHistoryStmt.setDouble(3, newBidAmount);
                insertHistoryStmt.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) { }
            return false;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) { }
        }
    }
    
    // get bid history cua item
    public List<BidTransactionDTO> getBidHistoryByItemId(int itemId) { 
        List<BidTransactionDTO> history = new ArrayList<>();
        String sql = "SELECT b.id, b.bidder_id, u.username, b.bid_amount, b.bid_time " +
                     "FROM bids b JOIN users u ON b.bidder_id = u.id WHERE b.item_id = ? ORDER BY b.bid_time DESC";
        
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try(PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            try(ResultSet rs = pstmt.executeQuery()) {
                while(rs.next()) {
                    int id = rs.getInt("id");
                    int bidderId = rs.getInt("bidder_id");
                    String bidderUsername = rs.getString("username");
                    double bidAmount = rs.getDouble("bid_amount");
                    LocalDateTime timestamp = rs.getTimestamp("bid_time").toLocalDateTime();
                    history.add(new BidTransactionDTO(id, bidderId, bidderUsername, bidAmount, timestamp));
                }
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return history; 
    }

    /**
     * Lấy danh sách các sản phẩm mà một người dùng đang tham gia đấu giá (trạng thái RUNNING).
     * @param userId ID của người dùng.
     * @return Danh sách các đối tượng Item.
     */
    public List<Item> getActiveBidsByUserId(int userId) {
        List<Item> activeBidItems = new ArrayList<>();
        // Lấy các item_id duy nhất mà user đã bid, sau đó join để lấy thông tin item đang RUNNING
        String sql = "SELECT i.* FROM items i " +
                     "JOIN (SELECT DISTINCT item_id FROM bids WHERE bidder_id = ?) b " +
                     "ON i.id = b.item_id " +
                     "WHERE i.status = 'RUNNING' " +
                     "ORDER BY i.end_time ASC";

        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Cần một hàm mapRowToItem trong ItemDAO, hoặc định nghĩa một hàm tương tự ở đây
                    // Tạm thời, chúng ta sẽ tạo một ItemDAO instance để sử dụng hàm map của nó
                    // (Đây là một cách làm nhanh, trong thực tế có thể cấu trúc lại để dùng chung)
                    activeBidItems.add(mapRowToItem(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return activeBidItems;
    }

    // Hàm phụ trợ để map ResultSet sang Item, tránh phụ thuộc vào ItemDAO
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
}
