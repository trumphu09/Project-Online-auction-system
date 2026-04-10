package com.auction.server.dao;

import com.auction.server.models.AutoBid;
import com.auction.server.models.Bidder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AutoBidDAO {

    // Add new auto-bid
    public boolean addAutoBid(int itemId, int bidderId, double maxAmount, double increment) {
        String sql = "INSERT INTO auto_bids (item_id, bidder_id, max_bid_amount, increment_amount) VALUES (?, ?, ?, ?)";

        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            pstmt.setInt(2, bidderId);
            pstmt.setDouble(3, maxAmount);
            pstmt.setDouble(4, increment);

            int rowsInserted = pstmt.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get all active auto-bids for an item
    public List<AutoBid> getActiveAutoBidsForItem(int itemId) {
        String sql = "SELECT ab.id, ab.max_bid_amount, ab.increment_amount, ab.created_at, " +
                    "u.username, b.account_balance " +
                    "FROM auto_bids ab " +
                    "JOIN bidders b ON ab.bidder_id = b.user_id " +
                    "JOIN users u ON b.user_id = u.id " +
                    "WHERE ab.item_id = ? AND ab.is_active = TRUE";

        List<AutoBid> autoBids = new ArrayList<>();
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Create bidder object
                    Bidder bidder = new Bidder(rs.getInt("ab.bidder_id"), rs.getString("username"),
                                             "password", "email", rs.getDouble("account_balance"));

                    AutoBid autoBid = new AutoBid(rs.getInt("ab.id"), bidder,
                                                rs.getDouble("max_bid_amount"),
                                                rs.getDouble("increment_amount"));
                    autoBids.add(autoBid);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return autoBids;
    }

    // Deactivate auto-bid
    public boolean deactivateAutoBid(int autoBidId) {
        String sql = "UPDATE auto_bids SET is_active = FALSE WHERE id = ?";

        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, autoBidId);

            int rowsUpdated = pstmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Remove auto-bid for a bidder on specific item
    public boolean removeAutoBid(int itemId, int bidderId) {
        String sql = "DELETE FROM auto_bids WHERE item_id = ? AND bidder_id = ?";

        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            pstmt.setInt(2, bidderId);

            int rowsDeleted = pstmt.executeUpdate();
            return rowsDeleted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}