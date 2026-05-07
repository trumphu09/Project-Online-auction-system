package com.auction.service;

import com.auction.server.dao.BidsDAO;
import com.auction.server.dao.ItemDAO;
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

    public List<Item> getActiveBids(int userId) {
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
        String sqlGetAutoBid = "SELECT user_id, max_amount FROM auto_bids WHERE auction_id = ? AND user_id != ? AND max_amount > ? ORDER BY max_amount DESC LIMIT 1";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlGetAutoBid)) {
            
            pstmt.setInt(1, auctionId);
            pstmt.setInt(2, currentBidderId);
            pstmt.setDouble(3, currentPrice);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int autoBidderId = rs.getInt("user_id");
                    double maxAmount = rs.getDouble("max_amount");
                    
                    double priceStep = 50000.0;
                    double newAutoPrice = currentPrice + priceStep;
                    
                    if (newAutoPrice <= maxAmount) {
                        System.out.println("🤖 AUTO-BID Kích hoạt! User " + autoBidderId + " tự động đè giá lên " + newAutoPrice);
                        processBid(auctionId, autoBidderId, newAutoPrice);
                    } else if (currentPrice < maxAmount) {
                        System.out.println("🤖 AUTO-BID All-in! User " + autoBidderId + " chốt giá trần " + maxAmount);
                        processBid(auctionId, autoBidderId, maxAmount);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi chạy Auto-Bid: " + e.getMessage());
        }
    }
}