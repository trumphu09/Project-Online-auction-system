package com.auction.server.dao;

import java.sql.*;
import java.time.LocalDateTime;

public class BidDAO {

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
    

}
