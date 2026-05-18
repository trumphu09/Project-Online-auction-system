package com.auction.server.dao;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.auction.server.models.BidTransactionDTO;
import com.auction.server.models.Item;
import com.auction.server.models.ItemDTO;
import com.auction.server.models.AuctionStatus;
import java.time.Duration;
import java.time.LocalDateTime;

public class BidsDAO {

    public static class BidResult {
    private final boolean success;
    private final boolean timeExtended;
    private final LocalDateTime newEndTime;
    private final String message;

    public BidResult(boolean success, boolean timeExtended, LocalDateTime newEndTime, String message) {
            this.success = success;
            this.timeExtended = timeExtended;
            this.newEndTime = newEndTime;
            this.message = message;
    }

        public boolean isSuccess() { return success; }
        public boolean isTimeExtended() { return timeExtended; }
        public LocalDateTime getNewEndTime() { return newEndTime; }
        public String getMessage() { return message; }
    }

    // Thực hiện đặt giá (giữ nguyên logic nhưng đảm bảo try-with-resources và exception handling)
    // Giữ tương thích cũ nếu nơi khác vẫn gọi boolean
    public boolean executeBid(int auctionId, int newBidderId, double newBidAmount) {
        return executeBidDetailed(auctionId, newBidderId, newBidAmount).isSuccess();
    }

    // Hàm mới: trả về cả trạng thái gia hạn
    public BidResult executeBidDetailed(int auctionId, int newBidderId, double newBidAmount) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            conn.setAutoCommit(false);

            String checkItemSql =
                "SELECT current_max_price, highest_bidder_id, status, end_time " +
                "FROM auctions WHERE id = ? FOR UPDATE";

            double currentMaxPrice;
            int oldBidderId;
            LocalDateTime endTime;

            try (PreparedStatement checkStmt = conn.prepareStatement(checkItemSql)) {
                checkStmt.setInt(1, auctionId);

                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return new BidResult(false, false, null, "Thất bại: Không tìm thấy phiên đấu giá!");
                    }

                    String status = rs.getString("status");
                    if (!"RUNNING".equals(status) && !"OPEN".equals(status)) {
                        conn.rollback();
                        return new BidResult(false, false, null, "Thất bại: Phiên đấu giá không còn mở!");
                    }

                    currentMaxPrice = rs.getDouble("current_max_price");
                    oldBidderId = rs.getInt("highest_bidder_id");

                    Timestamp endTs = rs.getTimestamp("end_time");
                    if (endTs == null) {
                        conn.rollback();
                        return new BidResult(false, false, null, "Thất bại: end_time không hợp lệ!");
                    }
                    endTime = endTs.toLocalDateTime();
                }
            }

            // 1) Chặn bid quá giờ ngay trong transaction
            LocalDateTime now = LocalDateTime.now();
            long remainingSeconds = Duration.between(now, endTime).getSeconds();

            if (remainingSeconds <= 0) {
                conn.rollback();
                return new BidResult(false, false, null, "Thất bại: Phiên đấu giá đã hết giờ!");
            }

            // 2) Giữ nguyên logic giá hiện có
            if (newBidAmount <= currentMaxPrice) {
                conn.rollback();
                return new BidResult(false, false, null, "Thất bại: Giá đặt phải cao hơn giá hiện tại!");
            }

            if (oldBidderId == newBidderId) {
                double difference = newBidAmount - currentMaxPrice;
                String deductDiffSql =
                    "UPDATE bidders SET account_balance = account_balance - ? " +
                    "WHERE user_id = ? AND account_balance >= ?";

                try (PreparedStatement deductStmt = conn.prepareStatement(deductDiffSql)) {
                    deductStmt.setDouble(1, difference);
                    deductStmt.setInt(2, newBidderId);
                    deductStmt.setDouble(3, difference);

                    if (deductStmt.executeUpdate() == 0) {
                        conn.rollback();
                        return new BidResult(false, false, null, "Thất bại: Ví không đủ tiền để trả phần chênh lệch!");
                    }
                }
            } else {
                if (oldBidderId != 0) {
                    String refundSql = "UPDATE bidders SET account_balance = account_balance + ? WHERE user_id = ?";
                    try (PreparedStatement refundStmt = conn.prepareStatement(refundSql)) {
                        refundStmt.setDouble(1, currentMaxPrice);
                        refundStmt.setInt(2, oldBidderId);
                        refundStmt.executeUpdate();
                    }
                }

                String deductSql =
                    "UPDATE bidders SET account_balance = account_balance - ? " +
                    "WHERE user_id = ? AND account_balance >= ?";

                try (PreparedStatement deductStmt = conn.prepareStatement(deductSql)) {
                    deductStmt.setDouble(1, newBidAmount);
                    deductStmt.setInt(2, newBidderId);
                    deductStmt.setDouble(3, newBidAmount);

                    if (deductStmt.executeUpdate() == 0) {
                        conn.rollback();
                        return new BidResult(false, false, null, "Thất bại: Số dư không đủ!");
                    }
                }
            }

            // 3) Anti-sniping: còn <= 15 giây thì cộng thêm 1 phút
            boolean timeExtended = false;
            LocalDateTime newEndTime = endTime;
            if (remainingSeconds > 0 && remainingSeconds <= 15) {
                timeExtended = true;
                newEndTime = endTime.plusMinutes(1);
            }

            // 4) Update auction trong cùng transaction
            String updateAuctionSql;
            if (timeExtended) {
                updateAuctionSql =
                    "UPDATE auctions " +
                    "SET current_max_price = ?, highest_bidder_id = ?, end_time = ?, has_extended = 1 " +
                    "WHERE id = ?";
            } else {
                updateAuctionSql =
                    "UPDATE auctions " +
                    "SET current_max_price = ?, highest_bidder_id = ? " +
                    "WHERE id = ?";
            }

            try (PreparedStatement updateStmt = conn.prepareStatement(updateAuctionSql)) {
                updateStmt.setDouble(1, newBidAmount);
                updateStmt.setInt(2, newBidderId);

                if (timeExtended) {
                    updateStmt.setTimestamp(3, Timestamp.valueOf(newEndTime));
                    updateStmt.setInt(4, auctionId);
                } else {
                    updateStmt.setInt(3, auctionId);
                }

                if (updateStmt.executeUpdate() == 0) {
                    conn.rollback();
                    return new BidResult(false, false, null, "Thất bại: Không thể cập nhật phiên đấu giá!");
                }
            }

            // 5) Lưu lịch sử bid
            String insertHistorySql =
                "INSERT INTO bids (auction_id, bidder_id, bid_amount) VALUES (?, ?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertHistorySql)) {
                insertStmt.setInt(1, auctionId);
                insertStmt.setInt(2, newBidderId);
                insertStmt.setDouble(3, newBidAmount);
                insertStmt.executeUpdate();
            }

            conn.commit();

            String msg = timeExtended
                ? "Thành công: Đã đặt giá và gia hạn thêm 1 phút do sắp hết giờ!"
                : "Thành công: Đã đặt giá!";
            return new BidResult(true, timeExtended, newEndTime, msg);

        } catch (SQLException e) {
            e.printStackTrace();
            return new BidResult(false, false, null, "Thất bại: Lỗi hệ thống khi đặt giá!");
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