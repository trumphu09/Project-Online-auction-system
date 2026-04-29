package com.auction.server.models;
import java.time.Duration;
import java.time.LocalDateTime;
import com.auction.service.AuctionService; // Import AuctionService
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class AuctionTimer {

    private static volatile AuctionTimer instance;
    private final ScheduledExecutorService scheduler;
    private AuctionManager auctionManager; // This will be set by AuctionManager
    private final AuctionService auctionService; // Add AuctionService instance

    private AuctionTimer() {

        // Sử dụng ThreadPool 1 luồng duy nhất chạy ngầm để tiết kiệm tài nguyên
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.auctionService = AuctionService.getInstance(); // Initialize AuctionService

    }

    public static AuctionTimer getInstance() {
        if (instance == null) {
            synchronized (AuctionTimer.class) {
                if (instance == null) {
                    instance = new AuctionTimer();

                }
            }

        }
        return instance;
    }


    public void setAuctionManager(AuctionManager auctionManager) {
        this.auctionManager = auctionManager;

    }

    // Bắt đầu chạy bộ quét thời gian định kỳ (1 giây / lần)

    public void start() {
        if (auctionManager == null) {
            throw new IllegalStateException("Cần setAuctionManager trước khi gọi start()!");

        }

        scheduler.scheduleAtFixedRate(() -> {
            try {
                checkExpiredAuctions();
            } catch (Exception e) {
                System.err.println("Lỗi trong quá trình quét thời gian đấu giá: " + e.getMessage());
            }

        }, 1, 1, TimeUnit.SECONDS);

        System.out.println("⏰ AuctionTimer đã khởi động, tự động quét định kỳ mỗi 1 giây.");

    }

    // Quét và đóng các phiên đấu giá đã hết hạn

    private void checkExpiredAuctions() {
        List<Auction> auctions = auctionManager.getAllAuctions();
        LocalDateTime now = LocalDateTime.now();

        for (Auction auction : auctions) {
            if ((auction.getStatus() == AuctionStatus.OPEN || auction.getStatus() == AuctionStatus.RUNNING)
                    && now.isAfter(auction.getEndTime())) {
                System.out.println("\n⏳ HẾT GIỜ: Phiên đấu giá [" + auction.getId() + "] đã hết hạn. Đang xử lý đóng phiên...");
                
                // Gọi AuctionService để đóng phiên đấu giá và xử lý thanh toán/hủy bỏ trong DB
                AuctionStatus finalStatus = auctionService.closeAuction(auction.getId());

                if (finalStatus != null) {
                    // Cập nhật trạng thái của đối tượng Auction trong bộ nhớ
                    auction.setStatus(finalStatus);
                    System.out.println("✅ Phiên đấu giá [" + auction.getId() + "] đã được đóng với trạng thái: " + finalStatus);
                } else {
                    System.err.println("❌ Lỗi khi đóng phiên đấu giá [" + auction.getId() + "]. Trạng thái không được cập nhật.");
                }

                // Thông báo qua WebSocket (hoặc Listener) để Client biết phiên đã kết thúc
                auctionManager.notifyAuctionUpdate(auction);
            }
        }
    }

    // Dừng luồng quét (Gọi khi shutdown Server)
    public void stop() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
            }
            System.out.println("🛑 AuctionTimer đã dừng hoàn toàn.");
        }
    }

    // [Tiện ích] Lấy thời gian đếm ngược định dạng HH:mm:ss cho 1 phiên cụ thể
    public static String getRemainingTimeFormatted(Auction auction) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(auction.getEndTime())) {
            return "00:00:00";
        }

        Duration duration = Duration.between(now, auction.getEndTime());
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);

    }

} 