package com.auction.server.scheduler;

import com.auction.server.dao.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AuctionStatusScheduler {
    private ScheduledExecutorService scheduler;

    public void start() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        // Cứ mỗi 1 phút (60 giây), chạy hàm kiểm tra 1 lần
        scheduler.scheduleAtFixedRate(this::checkAndSyncAuctionStatuses, 0, 1, TimeUnit.MINUTES);
        System.out.println("[SCHEDULER] Hệ thống tự động quản lý phiên đấu giá đã khởi động!");
    }

    private void checkAndSyncAuctionStatuses() {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            
            // 1. KÍCH HOẠT: Đồ nào đến giờ bắt đầu -> Đổi từ PENDING sang RUNNING
            String startSql = "UPDATE auctions SET status = 'RUNNING' WHERE status = 'OPEN' AND start_time <= NOW()";
            try (PreparedStatement startStmt = conn.prepareStatement(startSql)) {
                int started = startStmt.executeUpdate();
                if (started > 0) System.out.println("[SCHEDULER] Đã mở " + started + " phiên đấu giá mới.");
            }

            // 2. CHỐT SỔ: Đồ nào qua giờ kết thúc -> Đổi từ RUNNING sang ENDED và CHUYỂN TIỀN
            String getEndedAuctionsSql = "SELECT a.id, a.current_max_price, a.highest_bidder_id, i.seller_id " +
                                         "FROM auctions a JOIN items i ON a.item_id = i.id " +
                                         "WHERE a.status = 'RUNNING' AND a.end_time <= NOW()";
            
            try (PreparedStatement getEndedStmt = conn.prepareStatement(getEndedAuctionsSql);
                 java.sql.ResultSet rs = getEndedStmt.executeQuery()) {
                
                while (rs.next()) {
                    int auctionId = rs.getInt("id");
                    double finalPrice = rs.getDouble("current_max_price");
                    int winnerId = rs.getInt("highest_bidder_id");
                    int sellerId = rs.getInt("seller_id");

                    conn.setAutoCommit(false); // Khóa lại để chuyển tiền an toàn

                    try {
                        // A. Cập nhật trạng thái thành ENDED
                        String closeAuctionSql = "UPDATE auctions SET status = 'FINISHED' WHERE id = ?";
                        try (PreparedStatement closeStmt = conn.prepareStatement(closeAuctionSql)) {
                            closeStmt.setInt(1, auctionId);
                            closeStmt.executeUpdate();
                        }

                        // B. Chuyển tiền cho Seller (Nếu có người mua)
                        if (winnerId != 0 && finalPrice > 0) {
                            String paySellerSql = "UPDATE sellers SET account_balance = account_balance + ? WHERE user_id = ?";
                            try (PreparedStatement payStmt = conn.prepareStatement(paySellerSql)) {
                                payStmt.setDouble(1, finalPrice);
                                payStmt.setInt(2, sellerId);
                                payStmt.executeUpdate();
                            }
                            System.out.println("💰 Đã chuyển " + finalPrice + " vào ví Seller " + sellerId);
                        } else {
                            System.out.println(" Phiên " + auctionId + " ế, không có ai mua.");
                        }

                        conn.commit(); // Thành công!
                    } catch (SQLException ex) {
                        conn.rollback();
                        System.err.println("Lỗi khi chốt phiên " + auctionId + ": " + ex.getMessage());
                    } finally {
                        conn.setAutoCommit(true);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }
    }
}