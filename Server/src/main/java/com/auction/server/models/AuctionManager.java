package com.auction.server.models;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CopyOnWriteArrayList;

public class AuctionManager {

    private static AuctionManager instance;
    private final List<User> users;
    private final List<Auction> auctions;
    private final ExecutorService auctionExecutor; // For concurrent auction processing
    private final List<AuctionUpdateListener> updateListeners; // For realtime updates

    private AuctionManager() {
        users = new ArrayList<>();
        auctions = new CopyOnWriteArrayList<>(); // Thread-safe
        auctionExecutor = Executors.newCachedThreadPool(); // Dynamic thread pool
        updateListeners = new CopyOnWriteArrayList<>();
        System.out.println(">> Hệ thống quản lý đấu giá đã khởi động với concurrent support!");
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
            // Process bid asynchronously for better concurrency
            auctionExecutor.submit(() -> {
                boolean success = auction.placeBid(bidder, amount);
                if (success) {
                    // Broadcast update to all listeners
                    notifyAuctionUpdate(auction);
                }
            });
            return true; // Return immediately, actual result will be handled async
        } else {
            System.out.println("Lỗi: Không tìm thấy phiên đấu giá " + auctionId);
            return false;
        }
    }

    // Realtime update methods
    public void addUpdateListener(AuctionUpdateListener listener) {
        updateListeners.add(listener);
    }

    public void removeUpdateListener(AuctionUpdateListener listener) {
        updateListeners.remove(listener);
    }

    private void notifyAuctionUpdate(Auction auction) {
        AuctionUpdate update = new AuctionUpdate(auction.getId(), auction.getHighestBid(),
                                                auction.getBidHistory().size(), auction.getEndTime());
        for (AuctionUpdateListener listener : updateListeners) {
            listener.onAuctionUpdate(update);
        }
    }

    // Auto-bidding management
    public void setupAutoBid(int auctionId, Bidder bidder, double maxAmount, double increment) {
        Auction auction = getAuctionById(auctionId);
        if (auction != null) {
            AutoBid autoBid = new AutoBid(auction.getAutoBids().size() + 1, bidder, maxAmount, increment);
            auction.addAutoBid(autoBid);
            System.out.println("Auto-bid setup for auction [" + auctionId + "] by [" + bidder.getUsername() + "]");
        }
    }

    // Cleanup method
    public void shutdown() {
        auctionExecutor.shutdown();
        System.out.println("AuctionManager shutdown completed.");
    }
}
