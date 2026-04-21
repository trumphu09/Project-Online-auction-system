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

public class BidsDAO {
    // Thực hiện đặt giá mới
    public boolean executeBid(int auctionId, int newBidderId, double newBidAmount) {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try {
            conn.setAutoCommit(false);

            String checkItemSql = "SELECT current_max_price, highest_bidder_id, status FROM auctions WHERE id = ? FOR UPDATE";
            double currentMaxPrice = 0;
            int oldBidderId = 0;
            
            try (PreparedStatement checkStmt = conn.prepareStatement(checkItemSql)) {
                checkStmt.setInt(1, auctionId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        String status = rs.getString("status");
                        if (!"RUNNING".equals(status) && !"OPEN".equals(status)) {
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

            String updateItemSql = "UPDATE auctions SET current_max_price = ?, highest_bidder_id = ? WHERE id = ?";
            try (PreparedStatement updateItemStmt = conn.prepareStatement(updateItemSql)) {
                updateItemStmt.setDouble(1, newBidAmount);
                updateItemStmt.setInt(2, newBidderId);
                updateItemStmt.setInt(3, auctionId);
                updateItemStmt.executeUpdate();
            }

            String insertHistorySql = "INSERT INTO bids (auction_id, bidder_id, bid_amount) VALUES (?, ?, ?)";
            try (PreparedStatement insertHistoryStmt = conn.prepareStatement(insertHistorySql)) {
                insertHistoryStmt.setInt(1, auctionId);
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
    public List<BidTransactionDTO> getBidHistoryByAuctionId(int auctionId) { 
    List<BidTransactionDTO> history = new ArrayList<>();
    String sql = "SELECT b.id, b.bidder_id, u.username, b.bid_amount, b.bid_time " +
                 "FROM bids b JOIN users u ON b.bidder_id = u.id WHERE b.auction_id = ? ORDER BY b.bid_time DESC";
    
    Connection conn = DatabaseConnection.getInstance().getConnection();
    try(PreparedStatement pstmt = conn.prepareStatement(sql)) {
        pstmt.setInt(1, auctionId);
        try(ResultSet rs = pstmt.executeQuery()) {
            while(rs.next()) {  // ✅ Lặp tất cả dòng
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


}
