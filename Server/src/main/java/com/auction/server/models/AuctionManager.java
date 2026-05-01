package com.auction.server.models;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Quản lý vòng đời và các hoạt động của các phiên đấu giá.
 * Tích hợp với AuctionTimer để tự động cập nhật trạng thái.
 * Sử dụng Singleton Pattern để đảm bảo chỉ có một instance duy nhất.
 */
public class AuctionManager {

    private static volatile AuctionManager instance; // volatile for thread-safety

    private final List<User> users;
    private final List<Auction> auctions; // Thread-safe list for auctions
    private final ExecutorService auctionExecutor; // For concurrent bid processing
    private final List<AuctionUpdateListener> updateListeners; // For realtime updates
    private final AuctionTimer auctionTimer; // The timer to scan auctions

    /**
     * Private constructor to follow Singleton pattern.
     * Initializes all components and starts the AuctionTimer.
     */
    private AuctionManager() {
        // 1. Initialize original components
        this.users = new CopyOnWriteArrayList<>();
        this.auctions = new CopyOnWriteArrayList<>();
        this.auctionExecutor = Executors.newCachedThreadPool();
        this.updateListeners = new CopyOnWriteArrayList<>();
        System.out.println(">> Hệ thống quản lý đấu giá đã khởi động.");

        // 2. Integrate AuctionTimer
        this.auctionTimer = AuctionTimer.getInstance();
        this.auctionTimer.setAuctionManager(this); // Link timer back to this manager
        this.auctionTimer.start(); // Start the timer
        System.out.println("✅ AuctionTimer đã được khởi tạo và đang chạy.");
    }

    public static AuctionManager getInstance() {
        // (1) Kiểm tra lần 1 (Không có khóa)
        if (instance == null) {

            // (2) Khóa class lại
            synchronized (AuctionManager.class) {

                // (3) Kiểm tra lần 2 (Đã bị khóa)
                if (instance == null) {
                    // (4) Khởi tạo đối tượng
                    instance = new AuctionManager();
                }
            }
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

    /**
     * Returns a list of all auctions.
     * This method is called by AuctionTimer to scan for status changes.
     * @return A thread-safe list of all auctions.
     */
    public List<Auction> getAllAuctions() {
        return this.auctions;
    }

    public Auction getAuctionById(int auctionId) {
        for (Auction a : auctions) {
            // Assuming Auction.getId() returns an int
            if (a.getId() == auctionId) {
                return a;
            }
        }
        return null; // Not found
    }

    /**
     * Processes a bid request asynchronously.
     */
    public boolean processBidRequest(int auctionId, Bidder bidder, double amount) {
        Auction auction = getAuctionById(auctionId);
        if (auction != null) {
            auctionExecutor.submit(() -> {
                boolean success = auction.placeBid(bidder, amount);
                if (success) {
                    notifyAuctionUpdate(auction); // Broadcast update to listeners
                }
            });
            return true; // Request submitted
        } else {
            System.out.println("Lỗi: Không tìm thấy phiên đấu giá " + auctionId);
            return false;
        }
    }

    // --- Realtime update listener methods ---
    public void addUpdateListener(AuctionUpdateListener listener) {
        updateListeners.add(listener);
    }

    public void removeUpdateListener(AuctionUpdateListener listener) {
        updateListeners.remove(listener);
    }

    /**
     * Notifies all listeners about an auction update.
     * This is called after a successful bid or by the AuctionTimer when a status changes.
     * @param auction The updated auction.
     */
    public void notifyAuctionUpdate(Auction auction) {
        // Assuming AuctionUpdate is a DTO for sending data to clients
        AuctionUpdate update = new AuctionUpdate(auction.getId(), auction.getHighestBid(),
                                                auction.getBidHistory().size(), auction.getEndTime());
    /**
     * Phương thức công khai để các Service có thể gọi và kích hoạt một thông báo.
     * @param type Loại sự kiện (ví dụ: "NEW_BID", "AUCTION_ENDED")
     * @param data Dữ liệu liên quan đến sự kiện
     */
    public void broadcastUpdate(String type, Object data) {
        AuctionUpdate update = new AuctionUpdate(type, data);
        for (AuctionUpdateListener listener : updateListeners) {
            listener.onAuctionUpdate(update);
        }
        System.out.println("📢 Thông báo cập nhật - Phiên đấu giá [" + auction.getId() + "], Trạng thái: " + auction.getStatus());
    }

    // --- Auto-bidding management ---
    public void setupAutoBid(int auctionId, Bidder bidder, double maxAmount, double increment) {
        Auction auction = getAuctionById(auctionId);
        if (auction != null) {
            // Assuming AutoBid class exists
            AutoBid autoBid = new AutoBid(auction.getAutoBids().size() + 1, bidder, maxAmount, increment);
            auction.addAutoBid(autoBid);
            System.out.println("Auto-bid đã được thiết lập cho phiên đấu giá [" + auctionId + "] bởi [" + bidder.getUsername() + "]");
        }
    }

    /**
     * Shuts down the manager and its related services gracefully.
     * This should be called when the application is closing.
     */
    public void shutdown() {
        System.out.println("... Đang tắt AuctionManager ...");
        if (this.auctionTimer != null) {
            this.auctionTimer.stop();
        }
        if (this.auctionExecutor != null) {
            this.auctionExecutor.shutdown();
        }
        System.out.println("✅ AuctionManager đã tắt hoàn toàn.");
    }
}
