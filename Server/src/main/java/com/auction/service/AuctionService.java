package com.auction.service;

import com.auction.server.dao.BidsDAO;
import com.auction.server.dao.BidsDAO.BidResult;
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
        }

        return result;
    }

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
