package com.auction.server.dao;
import com.auction.server.models.AuctionDataDTO;
import com.auction.server.models.AuctionStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class AuctionDAO {
    public int createAuction(int itemId, int sellerId, double priceStep,
                            LocalDateTime startTime, LocalDateTime endTime) {
        String sql =
                "INSERT INTO auctions " +
                "(item_id, seller_id, current_max_price, price_step, start_time, end_time, status, has_extended, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pst = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            pst.setInt(1, itemId);
            pst.setInt(2, sellerId);
            pst.setDouble(3, 0.0);
            pst.setDouble(4, priceStep > 0 ? priceStep : 1000.0);
            pst.setTimestamp(5, Timestamp.valueOf(startTime != null ? startTime : LocalDateTime.now()));
            pst.setTimestamp(6, Timestamp.valueOf(endTime != null ? endTime : LocalDateTime.now().plusDays(7)));
            pst.setString(7, AuctionStatus.OPEN.name());
            pst.setBoolean(8, false);
            pst.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));

            int affectedRows = pst.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi tạo phiên đấu giá: " + e.getMessage());
        }
        return -1;
    }

    public boolean updateAuctionStatus(int auctionId, AuctionStatus newStatus) {
        if (newStatus == null) return false;

        String sql = "UPDATE auctions SET status = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, newStatus.name());
            pst.setInt(2, auctionId);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi cập nhật trạng thái đấu giá: " + e.getMessage());
        }
        return false;
    }

    public boolean updateEndTimeAndExtension(int auctionId, LocalDateTime newEndTime, boolean hasExtended) {
        String sql = "UPDATE auctions SET end_time = ?, has_extended = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setTimestamp(1, Timestamp.valueOf(newEndTime));
            pst.setBoolean(2, hasExtended);
            pst.setInt(3, auctionId);
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi cập nhật gia hạn đấu giá: " + e.getMessage());
            return false;
        }
    }

    public AuctionDataDTO getAuctionDataById(int auctionId) {
        String sql =
                "SELECT item_id, seller_id, start_time, end_time, status, has_extended, " +
                "current_max_price, highest_bidder_id, price_step " +
                "FROM auctions WHERE id = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, auctionId);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    int itemId = rs.getInt("item_id");
                    int sellerId = rs.getInt("seller_id");

                    java.sql.Timestamp startTs = rs.getTimestamp("start_time");
                    java.sql.Timestamp endTs = rs.getTimestamp("end_time");

                    LocalDateTime startTime = startTs != null ? startTs.toLocalDateTime() : null;
                    LocalDateTime endTime = endTs != null ? endTs.toLocalDateTime() : null;

                    AuctionStatus status = AuctionStatus.valueOf(rs.getString("status"));
                    boolean hasExtended = rs.getBoolean("has_extended");

                    AuctionDataDTO dto = new AuctionDataDTO(auctionId, itemId, sellerId, endTime, status, hasExtended);
                    dto.setCurrentMaxPrice(rs.getDouble("current_max_price"));
                    dto.setHighestBidderId(rs.getInt("highest_bidder_id"));
                    dto.setPriceStep(rs.getDouble("price_step"));
                    dto.setStartTime(startTime != null ? startTime.toString() : null);

                    return dto;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi tìm kiếm phiên đấu giá: " + e.getMessage());
        }
        return null;
    }
}
