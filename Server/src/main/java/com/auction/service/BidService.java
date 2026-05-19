package com.auction.service;

import com.auction.server.dao.BidsDAO;
import com.auction.server.dao.ItemDAO;
import com.auction.server.models.ItemDTO;
import com.auction.server.dao.DatabaseConnection;
import com.auction.server.models.BidTransactionDTO; // Import đúng DTO của ông
import com.auction.server.models.Item;              // Import đúng Model của ông

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class BidService {
    // 1. CẤU HÌNH SINGLETON 
    private static BidService instance;
    private BidsDAO bidsDAO = new BidsDAO();
    private ItemDAO itemDAO = new ItemDAO();

    private BidService() {} 

    public static BidService getInstance() {
        if (instance == null) {
            instance = new BidService();
        }
        return instance;
    }

    // 2. CÁC HÀM XEM LỊCH SỬ (Gọi đúng chuẩn tên hàm đã có sẵn trong BidsDAO của nhóm)
    public List<BidTransactionDTO> getBidHistory(int itemId) {
        return bidsDAO.getBidHistoryByItemId(itemId); 
    }

    public List<ItemDTO> getActiveBids(int userId) {
        return bidsDAO.getActiveBidsByUserId(userId);
    }

    // 3. TỔNG TƯ LỆNH XỬ LÝ ĐẶT GIÁ 
    public boolean processBid(int auctionId, int bidderId, double amount) {
        boolean isSuccess = bidsDAO.executeBid(auctionId, bidderId, amount);
        
        if (isSuccess) {
            System.out.println("User " + bidderId + " đặt thành công " + amount + " vào phiên " + auctionId);

            boolean timeExtended = itemDAO.extendAuctionTimeIfNeeded(auctionId);
            if (timeExtended) {
                System.out.println("Đã gia hạn thêm thời gian cho phiên đấu giá " + auctionId);
            }

            triggerAutoBidLogic(auctionId, amount, bidderId);

            // Bắn WebSocket (Dùng getWebSocketServer() từ AuctionServer)
            com.auction.server.AuctionServer.getWebSocketServer().broadcastPriceUpdate(auctionId, amount);
            return true;
        }
        return false;
    }

    // 4. THUẬT TOÁN ĐẤU GIÁ TỰ ĐỘNG
    private void triggerAutoBidLogic(int auctionId, double currentPrice, int currentBidderId) {
        System.out.println("\n[BidService.triggerAutoBidLogic] Starting for auctionId=" + auctionId + 
            ", currentPrice=" + currentPrice + ", currentBidderId=" + currentBidderId);
            
        String sqlGetAutoBid = "SELECT user_id, max_amount, price_step FROM auto_bids " +
                "WHERE auction_id = ? AND user_id != ? AND max_amount > ? " +
                "ORDER BY max_amount DESC LIMIT 1";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlGetAutoBid)) {
            
            pstmt.setInt(1, auctionId);
            pstmt.setInt(2, currentBidderId);
            pstmt.setDouble(3, currentPrice);
            
            System.out.println("[BidService.triggerAutoBidLogic] Query params: auctionId=" + auctionId + 
                ", excludeUser=" + currentBidderId + ", minMaxAmount=" + currentPrice);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int autoBidderId = rs.getInt("user_id");
                    double maxAmount = rs.getDouble("max_amount");
                    double priceStep = rs.getDouble("price_step");
                    
                    System.out.println("[BidService.triggerAutoBidLogic] Found auto-bid: userId=" + autoBidderId + 
                        ", maxAmount=" + maxAmount + ", priceStep=" + priceStep);
                    
                    double newAutoPrice = currentPrice + priceStep;
                    
                    System.out.println("[BidService.triggerAutoBidLogic] Calculated newPrice=" + newAutoPrice + 
                        ", checking: newPrice (" + newAutoPrice + ") <= maxAmount (" + maxAmount + ") ?");
                    
                    if (newAutoPrice <= maxAmount) {
                        System.out.println("✅ AUTO-BID Kích hoạt! User " + autoBidderId + " → " + newAutoPrice);
                        processBid(auctionId, autoBidderId, newAutoPrice);
                    } else if (currentPrice < maxAmount) {
                        System.out.println("💰 AUTO-BID All-in! User " + autoBidderId + " → " + maxAmount);
                        processBid(auctionId, autoBidderId, maxAmount);
                    } else {
                        System.out.println("⛔ AUTO-BID BLOCKED: newPrice " + newAutoPrice + " > maxAmount " + maxAmount);
                    }
                } else {
                    System.out.println("[BidService.triggerAutoBidLogic] No auto-bid found for this auction");
                }
            }
        } catch (Exception e) {
            System.err.println("❌ ERROR in triggerAutoBidLogic: " + e.getMessage());
            e.printStackTrace();
        }
    }
}