package com.auction.server.dao;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.auction.server.models.BidTransactionDTO;
import com.auction.server.models.Item;
import com.auction.server.models.ItemDTO;
import com.auction.server.models.AuctionStatus;

public class BidsDAO {

    // Thực hiện đặt giá (giữ nguyên logic nhưng đảm bảo try-with-resources và exception handling)
    public boolean executeBid(int auctionId, int newBidderId, double newBidAmount) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            conn.setAutoCommit(false); // Khóa Transaction an toàn tuyệt đối

            String checkItemSql = "SELECT current_max_price, highest_bidder_id, status FROM auctions WHERE id = ? FOR UPDATE";
            double currentMaxPrice;
            int oldBidderId;

            try (PreparedStatement checkStmt = conn.prepareStatement(checkItemSql)) {
                checkStmt.setInt(1, auctionId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        String status = rs.getString("status");
                        if (!"RUNNING".equals(status) && !"OPEN".equals(status)) {
                            conn.rollback();
                            return false;
                        }
                        currentMaxPrice = rs.getDouble("current_max_price");
                        oldBidderId = rs.getInt("highest_bidder_id");
                    } else {
                        conn.rollback();
                        return false;
                    }
                }
            }

            if (newBidAmount <= currentMaxPrice) {
                conn.rollback();
                return false;
            }

            // === LOGIC HOÀN TIỀN VÀ TRỪ TIỀN CHUẨN MỰC ===
            if (oldBidderId == newBidderId) {
                // KỊCH BẢN 1: Tự outplay chính mình (Nâng giá bid của bản thân lên)
                // Phép thuật ở đây: Chỉ trừ đúng số tiền chênh lệch (newBidAmount - currentMaxPrice)
                double difference = newBidAmount - currentMaxPrice;
                String deductDiffSql = "UPDATE bidders SET account_balance = account_balance - ? WHERE user_id = ? AND account_balance >= ?";
                try (PreparedStatement deductStmt = conn.prepareStatement(deductDiffSql)) {
                    deductStmt.setDouble(1, difference);
                    deductStmt.setInt(2, newBidderId);
                    deductStmt.setDouble(3, difference);
                    if (deductStmt.executeUpdate() == 0) {
                        conn.rollback();
                        return false; // Ví không đủ trả phần chênh lệch
                    }
                }
            } else {
                // KỊCH BẢN 2: Tranh giá với người khác
                // BƯỚC 1: Hoàn tiền ngay lập tức cho người cũ (Giải phóng vốn)
                if (oldBidderId != 0) {
                    String refundSql = "UPDATE bidders SET account_balance = account_balance + ? WHERE user_id = ?";
                    try (PreparedStatement refundStmt = conn.prepareStatement(refundSql)) {
                        refundStmt.setDouble(1, currentMaxPrice);
                        refundStmt.setInt(2, oldBidderId);
                        refundStmt.executeUpdate();
                    }
                }

                // BƯỚC 2: Trừ toàn bộ tiền của người đặt mới
                String deductSql = "UPDATE bidders SET account_balance = account_balance - ? WHERE user_id = ? AND account_balance >= ?";
                try (PreparedStatement deductStmt = conn.prepareStatement(deductSql)) {
                    deductStmt.setDouble(1, newBidAmount);
                    deductStmt.setInt(2, newBidderId);
                    deductStmt.setDouble(3, newBidAmount);
                    if (deductStmt.executeUpdate() == 0) {
                        conn.rollback();
                        return false; // Người mới không đủ tiền
                    }
                }
            }
            // ===============================================

            // Cập nhật giá mới nhất và người thắng tạm thời vào phiên đấu giá
            String updateItemSql = "UPDATE auctions SET current_max_price = ?, highest_bidder_id = ? WHERE id = ?";
            try (PreparedStatement updateItemStmt = conn.prepareStatement(updateItemSql)) {
                updateItemStmt.setDouble(1, newBidAmount);
                updateItemStmt.setInt(2, newBidderId);
                updateItemStmt.setInt(3, auctionId);
                updateItemStmt.executeUpdate();
            }

            // Ghi lịch sử giao dịch (bids table)
            String insertHistorySql = "INSERT INTO bids (auction_id, bidder_id, bid_amount) VALUES (?, ?, ?)";
            try (PreparedStatement insertHistoryStmt = conn.prepareStatement(insertHistorySql)) {
                insertHistoryStmt.setInt(1, auctionId);
                insertHistoryStmt.setInt(2, newBidderId);
                insertHistoryStmt.setDouble(3, newBidAmount);
                insertHistoryStmt.executeUpdate();
            }

            conn.commit(); // Ghi toàn bộ thao tác xuống DB thành công
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Lấy lịch sử đấu giá theo itemId (join auctions -> bids -> users)
    public List<BidTransactionDTO> getBidHistoryByItemId(int itemId) {
        List<BidTransactionDTO> history = new ArrayList<>();
        String sql = "SELECT b.id, b.bidder_id, u.username, b.bid_amount, b.bid_time " +
                     "FROM bids b " +
                     "JOIN users u ON b.bidder_id = u.id " +
                     "JOIN auctions a ON b.auction_id = a.id " +
                     "WHERE a.item_id = ? " +
                     "ORDER BY b.bid_time DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, itemId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    int bidderId = rs.getInt("bidder_id");
                    String bidderUsername = rs.getString("username");
                    double bidAmount = rs.getDouble("bid_amount");
                    Timestamp ts = rs.getTimestamp("bid_time");
                    LocalDateTime timestamp = ts != null ? ts.toLocalDateTime() : null;
                    history.add(new BidTransactionDTO(id, bidderId, bidderUsername, bidAmount, timestamp));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }

    // Lấy lịch sử theo auctionId (nếu cần)
    public List<BidTransactionDTO> getBidHistoryByAuctionId(int auctionId) {
        List<BidTransactionDTO> history = new ArrayList<>();
        String sql = "SELECT b.id, b.bidder_id, u.username, b.bid_amount, b.bid_time " +
                     "FROM bids b JOIN users u ON b.bidder_id = u.id WHERE b.auction_id = ? ORDER BY b.bid_time DESC";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, auctionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    int bidderId = rs.getInt("bidder_id");
                    String bidderUsername = rs.getString("username");
                    double bidAmount = rs.getDouble("bid_amount");
                    Timestamp ts = rs.getTimestamp("bid_time");
                    LocalDateTime timestamp = ts != null ? ts.toLocalDateTime() : null;
                    history.add(new BidTransactionDTO(id, bidderId, bidderUsername, bidAmount, timestamp));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }

    /**
     * Lấy danh sách các sản phẩm mà một người dùng đang tham gia đấu giá (trạng thái RUNNING).
     */
    public List<ItemDTO> getActiveBidsByUserId(int userId) {
        List<ItemDTO> activeBidItems = new ArrayList<>();

        String sql = """
            SELECT DISTINCT
                i.id,
                i.seller_id,
                i.name,
                i.description,
                i.starting_price,
                i.category,
                i.created_at,
                i.image_path,
                a.id AS auction_id,
                a.current_max_price,
                a.highest_bidder_id,
                a.price_step,
                a.start_time,
                a.end_time,
                a.status AS auction_status
            FROM bids b
            JOIN auctions a ON b.auction_id = a.id
            JOIN items i ON a.item_id = i.id
            WHERE b.bidder_id = ?
            AND a.status IN ('OPEN', 'RUNNING')
            ORDER BY a.end_time DESC
            """;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ItemDTO item = new ItemDTO();
                    item.setId(rs.getInt("id"));
                    item.setSellerId(rs.getInt("seller_id"));
                    item.setName(rs.getString("name"));
                    item.setDescription(rs.getString("description"));
                    item.setStartingPrice(rs.getDouble("starting_price"));
                    item.setCategory(rs.getString("category"));
                    item.setCreatedAt(rs.getString("created_at"));
                    item.setImagePath(rs.getString("image_path"));

                    item.setAuctionId(rs.getInt("auction_id"));
                    item.setCurrentMaxPrice(rs.getDouble("current_max_price"));
                    item.setHighestBidderId(rs.getInt("highest_bidder_id"));
                    item.setPriceStep(rs.getDouble("price_step"));
                    item.setStartTime(rs.getTimestamp("start_time") != null
                            ? rs.getTimestamp("start_time").toString() : null);
                    item.setEndTime(rs.getTimestamp("end_time") != null
                            ? rs.getTimestamp("end_time").toString() : null);
                    item.setStatus(rs.getString("auction_status"));

                    activeBidItems.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return activeBidItems;
    }
    // Hàm phụ trợ để chuyển row -> Item (giữ tương thích với model Item trong project)
    private Item mapRowToItem(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int sellerId = rs.getInt("seller_id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        double startingPrice = rs.getDouble("starting_price");
        double currentMaxPrice = rs.getDouble("current_max_price");
        Timestamp startTs = rs.getTimestamp("start_time");
        Timestamp endTs = rs.getTimestamp("end_time");
        java.time.LocalDateTime startTime = startTs != null ? startTs.toLocalDateTime() : null;
        java.time.LocalDateTime endTime = endTs != null ? endTs.toLocalDateTime() : null;
        String statusStr = rs.getString("status");
        // Nếu model Item sử dụng enum khác, điều chỉnh mapping tương ứng
        AuctionStatus status = null;
        if (statusStr != null) {
            try {
                status = AuctionStatus.valueOf(statusStr);
            } catch (IllegalArgumentException ignored) { }
        }

        Item item = new Item(id, sellerId, name, description, startingPrice, startTime, endTime, rs.getString("category")) {
            @Override
            public void printInfo() {
                System.out.println("Item: " + name);
            }
        };

        item.setHighestBidderId(rs.getInt("highest_bidder_id"));
        item.setCurrentMaxPrice(currentMaxPrice);
        if (status != null) {
            item.setStatus(status);
        }
        return item;
    }
}