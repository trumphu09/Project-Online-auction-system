package com.auction.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class AutoBidDAO {
    private static final AutoBidDAO instance = new AutoBidDAO();
    public static AutoBidDAO getInstance() { return instance; }

    public boolean upsertAutoBid(int auctionId, int userId, double maxAmount, double priceStep) {
        String sql = """
            INSERT INTO auto_bids (auction_id, user_id, max_amount, price_step)
            VALUES (?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE max_amount = VALUES(max_amount), price_step = VALUES(price_step)
            """;
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, auctionId);
            ps.setInt(2, userId);
            ps.setDouble(3, maxAmount);
            ps.setDouble(4, priceStep);
            System.out.println("[AutoBidDAO.upsertAutoBid] Saving: auctionId=" + auctionId + 
                ", userId=" + userId + ", maxAmount=" + maxAmount + ", priceStep=" + priceStep);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("[AutoBidDAO.upsertAutoBid] ERROR: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Overload for backward compatibility
    public boolean upsertAutoBid(int auctionId, int userId, double maxAmount) {
        return upsertAutoBid(auctionId, userId, maxAmount, 50000.0);
    }

    public boolean removeAutoBid(int auctionId, int userId) {
        String sql = "DELETE FROM auto_bids WHERE auction_id = ? AND user_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, auctionId);
            ps.setInt(2, userId);
            int rowsAffected = ps.executeUpdate();
            System.out.println("[AutoBidDAO.removeAutoBid] Deleted " + rowsAffected + " rows for auctionId=" + auctionId + ", userId=" + userId);
            return rowsAffected > 0;
        } catch (Exception e) {
            System.err.println("[AutoBidDAO.removeAutoBid] ERROR: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public Map<String, Object> getAutoBidStatus(int auctionId, int userId) {
        String sql = "SELECT max_amount, price_step FROM auto_bids WHERE auction_id = ? AND user_id = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, auctionId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("active", true);
                    data.put("max_amount", rs.getDouble("max_amount"));
                    data.put("price_step", rs.getDouble("price_step"));
                    return data;
                }
            }
        } catch (Exception e) {
            System.err.println("[AutoBidDAO.getAutoBidStatus] ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        Map<String, Object> data = new HashMap<>();
        data.put("active", false);
        return data;
    }
}