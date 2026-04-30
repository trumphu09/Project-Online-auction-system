package com.auction.service;

import com.auction.server.dao.BidsDAO;
import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.UserDAO;
import com.auction.server.models.AuctionDataDTO;
import com.auction.server.models.AuctionManager;
import com.auction.server.models.AuctionStatus;
import com.auction.server.models.UserDTO;

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

    public String placeBid(int auctionId, int bidderId, double amount) {
        if (amount <= 0) return "Lỗi: Số tiền không hợp lệ!";
        
        boolean success = bidsDAO.executeBid(auctionId, bidderId, amount);
        
        if (success) {
            // Lấy thông tin để broadcast
            UserDTO bidder = userDAO.getUserById(bidderId);
            String bidderUsername = (bidder != null) ? bidder.getUsername() : "Một người dùng";

            Map<String, Object> bidData = new HashMap<>();
            bidData.put("auctionId", auctionId);
            bidData.put("newPrice", amount);
            bidData.put("bidderUsername", bidderUsername);

            // Thông báo cho AuctionManager
            AuctionManager.getInstance().broadcastUpdate("NEW_BID", bidData);

            return "Thành công: Đã chốt giá $" + amount;
        } else {
            return "Thất bại: Giá không đủ cao, phiên đã đóng, hoặc số dư không đủ!";
        }
    }

    public String closeAuction(int auctionId) {
        AuctionDataDTO auction = auctionDAO.getAuctionDataById(auctionId);
        if (auction == null) {
            return "Thất bại: Không tìm thấy phiên đấu giá #" + auctionId;
        }
        if (auction.getStatus() != AuctionStatus.RUNNING) {
            return "Thất bại: Phiên đấu giá này không ở trạng thái Đang chạy.";
        }

        if (auction.getHighestBidderId() > 0) {
            boolean isClosed = auctionDAO.updateAuctionStatus(auctionId, AuctionStatus.FINISHED);
            if (isClosed) {
                PaymentService paymentService = PaymentService.getInstance();
                String paymentResult = paymentService.createInvoice(
                    auctionId, 
                    auction.getHighestBidderId(), 
                    auction.getSellerId(), 
                    auction.getCurrentMaxPrice()
                );
                // Broadcast auction ended
                AuctionManager.getInstance().broadcastUpdate("AUCTION_ENDED", auction);
                return "Thành công: Đã chốt đơn. " + paymentResult;
            } else {
                return "Thất bại: Lỗi cơ sở dữ liệu khi đóng phiên đấu giá.";
            }
        } else {
            auctionDAO.updateAuctionStatus(auctionId, AuctionStatus.CANCELED);
            AuctionManager.getInstance().broadcastUpdate("AUCTION_ENDED", auction);
            return "Thành công: Đã đóng phiên đấu giá. Không có người mua.";
        }
    }
}
