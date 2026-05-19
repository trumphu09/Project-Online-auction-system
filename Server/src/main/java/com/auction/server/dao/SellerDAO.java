package com.auction.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.auction.server.models.Seller;
import com.auction.server.models.SellerDTO;

public class SellerDAO {
    private UserDAO userDAO = new UserDAO();

    public SellerDTO getSellerById(int sellerId){
        String sql = "SELECT u.id, u.username, u.email, s.account_balance, s.total_rating, s.sale_count " +
                     "FROM users u JOIN sellers s ON u.id = s.user_id WHERE u.id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, sellerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new SellerDTO(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getDouble("account_balance"),
                        rs.getDouble("total_rating"),
                        rs.getInt("sale_count")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getSellerById: " + e.getMessage());
        }
        return null;
    }

    // Transaction-safe rating update
    public boolean hasRatedSeller(int auctionId, int bidderId) {
        String sql = "SELECT 1 FROM seller_ratings WHERE auction_id = ? AND bidder_id = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, auctionId);
            ps.setInt(2, bidderId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Lỗi hasRatedSeller: " + e.getMessage());
            return false;
        }
    }

    public boolean rateSeller(int sellerId, int auctionId, int bidderId, double newScore) {
        newScore = Math.max(0.0, Math.min(5.0, newScore));

        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            String checkSql = "SELECT 1 FROM seller_ratings WHERE auction_id = ? AND bidder_id = ? FOR UPDATE";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, auctionId);
                checkStmt.setInt(2, bidderId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        conn.rollback();
                        return false;
                    }
                }
            }

            String insertSql = "INSERT INTO seller_ratings (auction_id, bidder_id, seller_id, rating) VALUES (?, ?, ?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setInt(1, auctionId);
                insertStmt.setInt(2, bidderId);
                insertStmt.setInt(3, sellerId);
                insertStmt.setDouble(4, newScore);
                if (insertStmt.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }
            }

            String lockSql = "SELECT total_rating, sale_count FROM sellers WHERE user_id = ? FOR UPDATE";
            double currentRating;
            int currentCount;
            try (PreparedStatement lockStmt = conn.prepareStatement(lockSql)) {
                lockStmt.setInt(1, sellerId);
                try (ResultSet rs = lockStmt.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return false;
                    }
                    currentRating = rs.getDouble("total_rating");
                    currentCount = rs.getInt("sale_count");
                }
            }

            double newAverage = ((currentRating * currentCount) + newScore) / (currentCount + 1);

            String updateSql = "UPDATE sellers SET total_rating = ?, sale_count = sale_count + 1 WHERE user_id = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setDouble(1, newAverage);
                updateStmt.setInt(2, sellerId);
                if (updateStmt.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Lỗi rateSeller: " + e.getMessage());
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ignored) {}
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException ignored) {}
        }
    }
}