package com.auction.service;

import com.auction.server.dao.AuctionDAO;
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

public class AuctionService {

    private static final AuctionService instance = new AuctionService();

    private final BidsDAO bidsDAO;
    private final AuctionDAO auctionDAO;
    private final UserDAO userDAO;

    private AuctionService() {
        this.bidsDAO = new BidsDAO();
        this.auctionDAO = new AuctionDAO();
        this.userDAO = new UserDAO();
    }

    public static AuctionService getInstance() {
        return instance;
    }

    // =========================================================
    // ĐẶT GIÁ - Entry point chính (từ PlaceBidAPI)
    // =========================================================
    public BidResult placeBid(int auctionId, int bidderId, double amount) {
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

            // ✅ FIX: Kích hoạt auto-bid sau mỗi lần đặt giá thành công
            triggerAutoBidLogic(auctionId, amount, bidderId);
        }

        return result;
    }

    // =========================================================
    // THUẬT TOÁN AUTO-BID (được gọi sau mỗi lần bid thành công)
    // =========================================================
    /**
     * Kiểm tra xem có người nào đã cài auto-bid với max_amount > currentPrice không.
     * Nếu có → tự động đặt giá thay họ (currentPrice + priceStep), sau đó đệ quy
     * để xử lý tiếp nếu có auto-bid chain.
     *
     * @param auctionId       ID phiên đấu giá
     * @param currentPrice    Giá hiện tại (vừa được đặt)
     * @param currentBidderId User vừa đặt giá (để tránh tự kích hoạt lại chính họ)
     */
    private void triggerAutoBidLogic(int auctionId, double currentPrice, int currentBidderId) {
        System.out.println("\n[AuctionService.triggerAutoBidLogic] auctionId=" + auctionId
                + ", currentPrice=" + currentPrice + ", currentBidderId=" + currentBidderId);

        // Lấy auto-bid có max_amount cao nhất (trừ người vừa bid)
        // và max_amount phải > currentPrice mới có thể đặt giá cao hơn
        String sql = "SELECT user_id, max_amount, price_step FROM auto_bids "
                + "WHERE auction_id = ? AND user_id != ? AND max_amount > ? "
                + "ORDER BY max_amount DESC LIMIT 1";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, auctionId);
            pstmt.setInt(2, currentBidderId);
            pstmt.setDouble(3, currentPrice);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("[AutoBid] Không tìm thấy auto-bid phù hợp → dừng.");
                    return;
                }

                int autoBidderId = rs.getInt("user_id");
                double maxAmount  = rs.getDouble("max_amount");
                double priceStep  = rs.getDouble("price_step");

                System.out.println("[AutoBid] Tìm thấy: userId=" + autoBidderId
                        + ", maxAmount=" + maxAmount + ", priceStep=" + priceStep);

                double newAutoPrice = currentPrice + priceStep;

                if (newAutoPrice <= maxAmount) {
                    // Bình thường: đặt thêm 1 bước giá
                    System.out.println("✅ [AutoBid] User " + autoBidderId
                            + " tự đặt giá → " + newAutoPrice);
                    placeBid(auctionId, autoBidderId, newAutoPrice);

                } else if (currentPrice < maxAmount) {
                    // All-in: không đủ 1 bước nhưng còn dư → đặt tối đa
                    System.out.println("💰 [AutoBid All-in] User " + autoBidderId
                            + " tự đặt tối đa → " + maxAmount);
                    placeBid(auctionId, autoBidderId, maxAmount);

                } else {
                    // maxAmount == currentPrice hoặc thấp hơn → không thể đặt
                    System.out.println("⛔ [AutoBid BLOCKED] maxAmount=" + maxAmount
                            + " không vượt được currentPrice=" + currentPrice);
                }
            }

        } catch (Exception e) {
            System.err.println("❌ [AutoBid ERROR] " + e.getMessage());
            e.printStackTrace();
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
}