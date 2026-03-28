package com.auction.server.models;
import java.util.ArrayList;
import java.util.List;

public class AuctionManager {

    private static AuctionManager instance;
    private final List<User> users;
    private final List<Auction> auctions;

    private AuctionManager() {
        users = new ArrayList<>();
        auctions = new ArrayList<>();
        System.out.println(">> Hệ thống quản lý đấu giá đã khởi động!");
    }

    public static AuctionManager getInstance() {
        if (instance == null) {
            instance = new AuctionManager();
        }
        return instance;
    }

    public void registerUser(User user) {
        users.add(user);
        System.out.println("Đã đăng ký người dùng vào hệ thống: " + user.getUsername());
    }

    public void createAuction(Auction auction) {
        auctions.add(auction);
        System.out.println("Đã đưa lên sàn phiên đấu giá: [" + auction.getId() + "] - " + auction.getItem().getName());
    }

    public List<Auction> getAllAuctions() {
        return auctions;
    }

    public Auction getAuctionById(int auctionId) {
        for (Auction a : auctions) {
            if (a.getId() == auctionId) {
                return a; // Tìm thấy
            }
        }
        return null; // Không tìm thấy
    }

    // Controller đứng ra nhận lệnh đặt giá
    public boolean processBidRequest(int auctionId, Bidder bidder, double amount) {
        Auction auction = getAuctionById(auctionId);
        if (auction != null) {
            // Chuyển việc xử lý chi tiết cho class Auction tự lo
            return auction.placeBid(bidder, amount);
        } else {
            System.out.println("Lỗi: Không tìm thấy phiên đấu giá " + auctionId);
            return false;
        }
    }
}