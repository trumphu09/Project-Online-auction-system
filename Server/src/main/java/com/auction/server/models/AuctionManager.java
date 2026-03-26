package com.auction.server.models;
<<<<<<< HEAD
import java.util.ArrayList;
import java.util.List;

public class AuctionManager {

=======
import java.util.List;
import java.util.ArrayList;
public class AuctionManager{
>>>>>>> a7c57267a2ad1bc874e3b227bfa0d30b8149305c
    private static AuctionManager instance;
    private final List<User> users;
    private final List<Auction> auctions;

<<<<<<< HEAD
    private AuctionManager() {
=======
    private AuctionManager(){
>>>>>>> a7c57267a2ad1bc874e3b227bfa0d30b8149305c
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
        System.out.println("Đã đưa lên sàn phiên đấu giá: [" + auction.getAuctionId() + "] - " + auction.getItem().getName());
    }

    public List<Auction> getAllAuctions() {
        return auctions;
    }

    public Auction getAuctionById(String auctionId) {
        for (Auction a : auctions) {
            if (a.getAuctionId().equals(auctionId)) {
                return a; // Tìm thấy
            }
        }
        return null; // Không tìm thấy
    }

<<<<<<< HEAD
    // Controller đứng ra nhận lệnh đặt giá
    public boolean processBidRequest(String auctionId, Bidder bidder, double amount) {
        Auction auction = getAuctionById(auctionId);
        if (auction != null) {
            // Chuyển việc xử lý chi tiết cho class Auction tự lo
=======
    public boolean processBidRequest(String auctionId, Bidder bidder, double amount) {
        Auction auction = getAuctionById(auctionId);
        if (auction != null) {
>>>>>>> a7c57267a2ad1bc874e3b227bfa0d30b8149305c
            return auction.placeBid(bidder, amount);
        } else {
            System.out.println("Lỗi: Không tìm thấy phiên đấu giá " + auctionId);
            return false;
        }
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> a7c57267a2ad1bc874e3b227bfa0d30b8149305c
