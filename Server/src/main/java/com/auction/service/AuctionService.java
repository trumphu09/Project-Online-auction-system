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

    /**
     * NGHIỆP VỤ: Chốt phiên đấu giá khi hết giờ
     */
    public String closeAuction(int auctionId) {
        // 1. Nhờ DAO lấy thông tin hiện tại của phiên đấu giá
        // (Giả sử bạn đã có hàm getAuctionById trong AuctionDAO)
        AuctionDataDTO auction = auctionDAO.getAuctionDataById(auctionId);

        if (auction == null) {
            System.out.println("Thất bại: Không tìm thấy phiên đấu giá #" + auctionId);
            return null; // Trả về null để báo hiệu lỗi
        }

        // Nếu phiên chưa chạy hoặc đã đóng rồi thì không làm gì cả
        if (!(auction.getStatus()==AuctionStatus.RUNNING)) {
            System.out.println("Thất bại: Phiên đấu giá #" + auctionId + " không đang chạy, không thể đóng.");
            return null; // Trả về null để báo hiệu lỗi
        }

        // 2. Kiểm tra xem có ai đặt giá không
        if (auction.getHighestBidderId() > 0) {
            
            // BƯỚC QUAN TRỌNG 1: Đổi trạng thái phiên đấu giá thành FINISHED (Đã kết thúc)
            // (Giả sử bạn đã có hàm updateStatus trong AuctionDAO)
            boolean isClosed = auctionDAO.updateAuctionStatus(auctionId, AuctionStatus.FINISHED);

            if (isClosed) {
                // BƯỚC QUAN TRỌNG 2: GỌI KẾ TOÁN TRƯỞNG TẠO HÓA ĐƠN TỰ ĐỘNG
                PaymentService paymentService = PaymentService.getInstance();
                
                // Lấy thông tin từ auction để ném sang cho PaymentService
                String paymentResult = paymentService.createInvoice(
                    auctionId, 
                    auction.getHighestBidderId(), 
                    auction.getSellerId(), 
                    auction.getCurrentMaxPrice()
                );

                System.out.println("Thành công: Đã chốt đơn cho người chơi #" + auction.getHighestBidderId() +
                                   ". Hệ thống Kế toán phản hồi: " + paymentResult);
                return AuctionStatus.FINISHED;
            } else {
                System.err.println("Thất bại: Lỗi cơ sở dữ liệu khi đóng phiên đấu giá #" + auctionId);
                return null; // Trả về null để báo hiệu lỗi
            }

        } else {
            // Trường hợp ế khách: Hết giờ mà không có ai đặt giá
            auctionDAO.updateAuctionStatus(auctionId, AuctionStatus.CANCELED); // Hoặc FINISHED tùy logic nhóm bạn
            System.out.println("Thành công: Đã đóng phiên đấu giá #" + auctionId + ". Không có người mua (Ế hàng!).");
            return AuctionStatus.CANCELED;
        }
    }
}
