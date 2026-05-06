package com.auction.service;

import com.auction.server.dao.BidderDAO;
import com.auction.server.models.Bidder;
import com.auction.server.models.BidderDTO;

public class BidderService {
    private static BidderService instance = new BidderService();
    private final BidderDAO bidderDAO;

    private BidderService(){
        this.bidderDAO = new BidderDAO();
    }

    public static BidderService getInstance() {
        return instance;
    }

    public BidderDTO getBidderProfile(int bidderId){
        if (bidderId <= 0) return null;
        return bidderDAO.getBidderById(bidderId);
    }

    public String depositMoney(int bidderId, double amount){
        if (amount <= 0) return "Thất bại: Số tiền nạp phải lớn hơn 0!";
        boolean success = bidderDAO.updateBalance(bidderId, amount);
        if (success) return "Thành công: nạp " + amount + "$ vào tài khoản.";
        return "Nạp tiền thất bại: Không tìm thấy tài khoản hoặc lỗi hệ thống!";
    }

    public String processPayment(int bidderId, double amount){
        if (amount <= 0) return "Thất bại: Số tiền thanh toán không hợp lệ!";

        BidderDTO currentBidder = bidderDAO.getBidderById(bidderId);
        if (currentBidder == null) {
            return "Thất bại: Không tìm thấy tài khoản!";
        }

        if (currentBidder.getAccountBalance() < amount) {
            return "Thất bại: Số dư không đủ! Bạn chỉ còn $" + currentBidder.getAccountBalance();
        }

        boolean success = bidderDAO.updateBalance(bidderId, -amount);
        if (success) {
            return "Thanh toán thành công: -" + amount + "$. Số dư mới: $" + (currentBidder.getAccountBalance() - amount);
        }
        return "Thanh toán thất bại: Lỗi hệ thống!";
    }
}