package com.auction.service;

import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.AutoBidDAO;
import com.auction.server.dao.BidsDAO;
import com.auction.server.dao.BidsDAO.BidResult;
import com.auction.server.dao.DatabaseConnection;
import com.auction.server.models.AuctionDataDTO;
import com.auction.server.models.AuctionManager;
import com.auction.server.models.AuctionStatus;
import com.auction.server.models.UserDTO;
import com.auction.server.dao.UserDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AuctionService {

    private static final AuctionService instance = new AuctionService();

    private final BidsDAO bidsDAO;
    private final AuctionDAO auctionDAO;
    private final UserDAO userDAO;
    private final AutoBidDAO autoBidDAO;

    private AuctionService() {
        this.bidsDAO = new BidsDAO();
        this.auctionDAO = new AuctionDAO();
        this.userDAO = new UserDAO();
        this.autoBidDAO = new AutoBidDAO();
    }

    public static AuctionService getInstance() {
        return instance;
    }

    // =========================================================
    // ĐẶT GIÁ - Entry point chính (từ PlaceBidAPI)
    // =========================================================
    public BidResult placeBid(int auctionId, int bidderId, double amount) {
        return placeBidInternal(auctionId, bidderId, amount, true);
    }

    private BidResult placeBidInternal(int auctionId, int bidderId, double amount, boolean triggerAutoBid) {
        if (amount <= 0) {
            return new BidResult(false, false, null, "Lỗi: Số tiền không hợp lệ!");
        }

        BidResult result = bidsDAO.executeBidDetailed(auctionId, bidderId, amount);

        if (result.isSuccess()) {
            UserDTO bidder = userDAO.getUserById(bidderId);
            String bidderUsername = (bidder != null) ? bidder.getUsername() : "Một người dùng";

            Map<String, Object> bidData = new HashMap<>();
            bidData.put("auctionId", auctionId);
            bidData.put("newPrice", amount);
            bidData.put("bidderUsername", bidderUsername);
            bidData.put("timeExtended", result.isTimeExtended());

            if (result.isTimeExtended() && result.getNewEndTime() != null) {
                String newEndTimeIso = result.getNewEndTime().toString();
                bidData.put("newEndTime", newEndTimeIso);

                Map<String, Object> extData = new HashMap<>();
                extData.put("auctionId", auctionId);
                extData.put("newEndTime", newEndTimeIso);
                extData.put("message", "Phiên đấu giá vừa được gia hạn thêm 1 phút!");
                AuctionManager.getInstance().broadcastUpdate("AUCTION_EXTENDED", extData);
            }

            AuctionManager.getInstance().broadcastUpdate("NEW_BID", bidData);

            // Chỉ resolve auto-bid khi đây là bid bên ngoài/manual.
            // Bid do resolver tạo ra thì không gọi lại để tránh loop.
            if (triggerAutoBid) {
                resolveAutoBidsAfterCurrentState(auctionId);
            }
        }

        return result;
    }

    // =========================================================
    // AUTO-BID RESOLUTION
    // =========================================================
    private boolean resolveAutoBidsAfterBid(int auctionId, double currentPrice, int currentLeaderId) {
        return resolveAutoBidsLoop(auctionId, currentPrice, currentLeaderId);
    }

    private boolean resolveAutoBidsLoop(int auctionId, double currentPrice, int currentLeaderId) {
        boolean executedAny = false;
        int safetyCounter = 0; // chặn loop vô hạn nếu dữ liệu lỗi

        while (safetyCounter++ < 50) {
            AutoBidCandidate candidate = findNextAutoBidCandidate(auctionId, currentLeaderId, currentPrice);
            if (candidate == null) {
                break;
            }

            double nextPrice = Math.min(currentPrice + candidate.priceStep, candidate.maxAmount);

            if (nextPrice <= currentPrice) {
                broadcastAutoBidNotice(
                        auctionId,
                        candidate.userId,
                        "Auto-bid đã dừng vì không thể nâng thêm giá."
                );
                break;
            }

            BidResult autoResult = bidsDAO.executeBidDetailed(auctionId, candidate.userId, nextPrice);
            if (!autoResult.isSuccess()) {
                broadcastAutoBidNotice(
                        auctionId,
                        candidate.userId,
                        "Auto-bid thất bại: " + autoResult.getMessage()
                );
                break;
            }

            executedAny = true;

            UserDTO bidder = userDAO.getUserById(candidate.userId);
            String bidderUsername = (bidder != null) ? bidder.getUsername() : "Auto-bid";

            broadcastBidEvent(auctionId, nextPrice, bidderUsername, autoResult);

            if (autoResult.isTimeExtended() && autoResult.getNewEndTime() != null) {
                broadcastAuctionExtended(auctionId, autoResult.getNewEndTime().toString());
            }

            currentPrice = nextPrice;
            currentLeaderId = candidate.userId;
        }

        return executedAny;
    }

    private AutoBidCandidate findNextAutoBidCandidate(int auctionId, int currentLeaderId, double currentPrice) {
        String sql = """
            SELECT user_id, max_amount, price_step
            FROM auto_bids
            WHERE auction_id = ?
              AND user_id <> ?
              AND max_amount > ?
            ORDER BY max_amount DESC, price_step DESC, user_id ASC
            LIMIT 1
            """;

        try (Connection conn = com.auction.server.dao.DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, auctionId);
            pstmt.setInt(2, currentLeaderId);
            pstmt.setDouble(3, currentPrice);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new AutoBidCandidate(
                            rs.getInt("user_id"),
                            rs.getDouble("max_amount"),
                            rs.getDouble("price_step")
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("[AuctionService.findNextAutoBidCandidate] ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private void broadcastBidEvent(int auctionId, double amount, String bidderUsername, BidResult result) {
        Map<String, Object> bidData = new HashMap<>();
        bidData.put("auctionId", auctionId);
        bidData.put("newPrice", amount);
        bidData.put("bidderUsername", bidderUsername);
        bidData.put("timeExtended", result != null && result.isTimeExtended());

        if (result != null && result.isTimeExtended() && result.getNewEndTime() != null) {
            String newEndTimeIso = result.getNewEndTime().toString();
            bidData.put("newEndTime", newEndTimeIso);

            Map<String, Object> extData = new HashMap<>();
            extData.put("auctionId", auctionId);
            extData.put("newEndTime", newEndTimeIso);
            extData.put("message", "Phiên đấu giá vừa được gia hạn thêm 1 phút!");
            AuctionManager.getInstance().broadcastUpdate("AUCTION_EXTENDED", extData);
        }

        AuctionManager.getInstance().broadcastUpdate("NEW_BID", bidData);
    }

    private void broadcastAuctionExtended(int auctionId, String newEndTimeIso) {
        Map<String, Object> extData = new HashMap<>();
        extData.put("auctionId", auctionId);
        extData.put("newEndTime", newEndTimeIso);
        extData.put("message", "Phiên đấu giá vừa được gia hạn thêm 1 phút!");
        AuctionManager.getInstance().broadcastUpdate("AUCTION_EXTENDED", extData);
    }

    private void broadcastAutoBidNotice(int auctionId, int userId, String message) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("auctionId", auctionId);
        payload.put("userId", userId);
        payload.put("message", message);
        AuctionManager.getInstance().broadcastUpdate("AUTO_BID_NOTICE", payload);
    }

    private static class AutoBidCandidate {
        final int userId;
        final double maxAmount;
        final double priceStep;

        AutoBidCandidate(int userId, double maxAmount, double priceStep) {
            this.userId = userId;
            this.maxAmount = maxAmount;
            this.priceStep = priceStep;
        }
    }
    // =========================================================
    // ĐÓNG PHIÊN ĐẤU GIÁ
    // =========================================================
    public AuctionStatus closeAuction(int auctionId) {
        AuctionDataDTO auction = auctionDAO.getAuctionDataById(auctionId);
        if (auction == null) {
            return null;
        }
        if (auction.getStatus() != AuctionStatus.RUNNING) {
            return auction.getStatus();
        }

        if (auction.getHighestBidderId() > 0) {
            boolean isClosed = auctionDAO.updateAuctionStatus(auctionId, AuctionStatus.FINISHED);
            if (isClosed) {
                PaymentService paymentService = PaymentService.getInstance();
                paymentService.createInvoice(
                        auctionId,
                        auction.getHighestBidderId(),
                        auction.getSellerId(),
                        auction.getCurrentMaxPrice()
                );
                AuctionManager.getInstance().broadcastUpdate("AUCTION_ENDED", auction);
                return AuctionStatus.FINISHED;
            } else {
                return null;
            }
        } else {
            auctionDAO.updateAuctionStatus(auctionId, AuctionStatus.CANCELED);
            AuctionManager.getInstance().broadcastUpdate("AUCTION_ENDED", auction);
            return AuctionStatus.CANCELED;
        }
    }
    private static class AutoBidRow {
        int userId;
        double maxAmount;
        double priceStep;
        LocalDateTime createdAt;

        AutoBidRow(int userId, double maxAmount, double priceStep, LocalDateTime createdAt) {
            this.userId = userId;
            this.maxAmount = maxAmount;
            this.priceStep = priceStep;
            this.createdAt = createdAt;
        }
    }

    /**
     * Resolver auto-bid deterministic với auto-bid war support:
     * 1 auto-bidder:
     *   - tự đặt current + step nếu không vượt max
     *   - user đó thành highest bidder ngay.
     *
     * >= 2 auto-bidder:
     *   - người maxAmount cao nhất thắng.
     *   - giá thắng = min(max người thắng, max người thua cao nhất + step)
     *   - hủy auto-bid người thua, gửi AUTO_BID_NOTICE để client reset UI.
     *   - tiếp tục loop để handle auto-bid war
     */
    public synchronized boolean resolveAutoBidsAfterCurrentState(int auctionId) {
        boolean anyBidPlaced = false;

        // Loop để xử lý auto-bid war
        int maxIterations = 100; // Tránh vô hạn loop
        while (maxIterations-- > 0) {
            boolean bidPlaced = resolveAutoBidsOnce(auctionId);
            if (bidPlaced) {
                anyBidPlaced = true;
            } else {
                break; // Không có bid nào được đặt, dừng loop
            }
        }

        return anyBidPlaced;
    }

    /**
     * Một lần resolver - xử lý một vòng auto-bid
     */
    private boolean resolveAutoBidsOnce(int auctionId) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {

            double currentPrice;
            int currentHighestBidderId;
            String status;

            String auctionSql = """
                SELECT current_max_price, highest_bidder_id, status
                FROM auctions
                WHERE id = ?
                """;

            try (PreparedStatement ps = conn.prepareStatement(auctionSql)) {
                ps.setInt(1, auctionId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return false;

                    currentPrice = rs.getDouble("current_max_price");
                    currentHighestBidderId = rs.getInt("highest_bidder_id");
                    status = rs.getString("status");
                }
            }

            if (!"RUNNING".equalsIgnoreCase(status) && !"OPEN".equalsIgnoreCase(status)) {
                return false;
            }

            List<AutoBidRow> rows = new ArrayList<>();

            String autoSql = """
                SELECT user_id, max_amount, price_step, created_at
                FROM auto_bids
                WHERE auction_id = ?
                AND max_amount > ?
                ORDER BY max_amount DESC, created_at ASC, user_id ASC
                """;

            try (PreparedStatement ps = conn.prepareStatement(autoSql)) {
                ps.setInt(1, auctionId);
                ps.setDouble(2, currentPrice);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Timestamp ts = rs.getTimestamp("created_at");
                        rows.add(new AutoBidRow(
                                rs.getInt("user_id"),
                                rs.getDouble("max_amount"),
                                rs.getDouble("price_step") > 0 ? rs.getDouble("price_step") : 50000.0,
                                ts != null ? ts.toLocalDateTime() : LocalDateTime.now()
                        ));
                    }
                }
            }

            if (rows.isEmpty()) {
                return false;
            }

            // Sort by maxAmount DESC, createdAt ASC, userId ASC
            rows.sort(
                    Comparator.comparingDouble((AutoBidRow r) -> r.maxAmount).reversed()
                            .thenComparing(r -> r.createdAt)
                            .thenComparingInt(r -> r.userId)
            );

            AutoBidRow winner = rows.get(0);
            double step = winner.priceStep > 0 ? winner.priceStep : 50000.0;

            double targetPrice;

            System.out.println("[AutoBid Debug] auctionId=" + auctionId + 
                    ", currentPrice=" + currentPrice + 
                    ", autoBidCount=" + rows.size() + 
                    ", winner.userId=" + winner.userId + 
                    ", winner.maxAmount=" + winner.maxAmount);

            if (rows.size() == 1) {
                // Case 1: chỉ có 1 auto-bidder
                // Nếu người này đã là highest bidder với bid amount >= current + step thì không cần bid lại
                if (winner.userId == currentHighestBidderId) {
                    // Họ đã giữ giá cao nhất - không cần action
                    System.out.println("[AutoBid] Winner " + winner.userId + " is already highest bidder");
                    return false;
                }

                // Người này chưa là highest bidder, hãy đặt giá cho họ
                targetPrice = Math.min(winner.maxAmount, currentPrice + step);
                System.out.println("[AutoBid Case 1] Setting bid: targetPrice=" + targetPrice);

            } else {
                // Case 2: có 2+ auto-bidder - người thắng là người maxAmount cao nhất
                AutoBidRow second = rows.get(1);
                
                // Giá thắng = min(winner.maxAmount, second.maxAmount + step)
                // Nhưng phải >= currentPrice + step để đảm bảo increment
                double minimumBid = Math.max(currentPrice + step, second.maxAmount + step);
                targetPrice = Math.min(winner.maxAmount, minimumBid);

                System.out.println("[AutoBid Case 2] Winner vs Second: " + 
                        winner.maxAmount + " vs " + second.maxAmount + 
                        ", targetPrice=" + targetPrice);

                // Hủy toàn bộ auto-bid của người thua (không phải người thắng)
                for (int i = 1; i < rows.size(); i++) {
                    AutoBidRow loser = rows.get(i);
                    autoBidDAO.removeAutoBid(auctionId, loser.userId);

                    Map<String, Object> notice = new HashMap<>();
                    notice.put("auctionId", auctionId);
                    notice.put("userId", loser.userId);
                    notice.put("message",
                            "Auto-bid của bạn đã bị hủy vì có người đặt max bid cao hơn (" + winner.maxAmount + "đ > " + loser.maxAmount + "đ). " +
                            "Nút auto-bid và ô nhập giá đã được reset.");

                    AuctionManager.getInstance().broadcastUpdate("AUTO_BID_NOTICE", notice);
                    System.out.println("[AutoBid] Cancelled auto-bid for user " + loser.userId);
                }
            }

            // Kiểm tra điều kiện hợp lệ trước khi đặt giá
            if (targetPrice <= currentPrice) {
                System.out.println("[AutoBid] Invalid: targetPrice (" + targetPrice + ") <= currentPrice (" + currentPrice + ")");
                return false;
            }

            if (targetPrice > winner.maxAmount) {
                System.out.println("[AutoBid] Invalid: targetPrice (" + targetPrice + ") > winner.maxAmount (" + winner.maxAmount + ")");
                return false;
            }

            // Đặt giá cho người thắng - KHÔNG trigger auto-bid để tránh loop
            BidResult bidResult = placeBidInternal(auctionId, winner.userId, targetPrice, false);

            if (bidResult.isSuccess()) {
                String messagePrefix = (rows.size() == 1) ? "Auto-bid được kích hoạt" : "Auto-bid war resolved";
                Map<String, Object> notice = new HashMap<>();
                notice.put("auctionId", auctionId);
                notice.put("userId", winner.userId);
                notice.put("message", messagePrefix + ". Bạn hiện là người giữ giá cao nhất (" + targetPrice + "đ).");

                AuctionManager.getInstance().broadcastUpdate("AUTO_BID_NOTICE", notice);
                System.out.println("[AutoBid] SUCCESS: User " + winner.userId + " bid at " + targetPrice);
                return true;
            } else {
                System.out.println("[AutoBid] FAILED to place bid: " + bidResult.getMessage());
                return false;
            }

        } catch (Exception e) {
            System.err.println("[AutoBid Resolver ERROR] " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}