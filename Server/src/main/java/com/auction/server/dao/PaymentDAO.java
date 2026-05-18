package com.auction.server.dao;

import com.auction.server.models.Payment;
import com.auction.server.models.PaymentStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO {

    // 1. TẠO HÓA ĐƠN
    public boolean createPaymentInvoice(int auctionId, int fromBidder, int toSeller, double amount) {
        String sql = "INSERT INTO payments (auction_id, from_bidder_id, to_seller_id, amount, status) VALUES (?, ?, ?, ?, 'PENDING')";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setInt(1, auctionId);
            pstmt.setInt(2, fromBidder);
            pstmt.setInt(3, toSeller);
            pstmt.setDouble(4, amount);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi createPaymentInvoice: " + e.getMessage());
            return false;
        }
    }

    // 2. XỬ LÝ THANH TOÁN
    // CHỈ CẦN DÙNG DUY NHẤT HÀM NÀY ĐỂ THANH TOÁN
    public boolean processPayment(int auctionId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false); // Bắt đầu Transaction

            // 1. Lấy thông tin VÀ TRẠNG THÁI HIỆN TẠI (Bỏ điều kiện cứng status='FINISHED' ở WHERE)
            String getAuctionInfoSql = "SELECT current_max_price, highest_bidder_id, seller_id, status FROM auctions WHERE id = ? FOR UPDATE";
            double amount = 0;
            int bidderId = 0, sellerId = 0;
            
            try (PreparedStatement checkStmt = conn.prepareStatement(getAuctionInfoSql)) {
                checkStmt.setInt(1, auctionId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        String currentStatus = rs.getString("status");
                        
                        // FIX SPAM CLICK: Nếu DB đã là PAID rồi, báo thành công luôn để UI không bị quăng lỗi!
                        if ("PAID".equalsIgnoreCase(currentStatus)) {
                            conn.rollback();
                            conn.setAutoCommit(true);
                            return true; 
                        }
                        
                        // Nếu chưa thanh toán thì bắt buộc phải là FINISHED mới cho đi tiếp
                        if (!"FINISHED".equalsIgnoreCase(currentStatus)) {
                            System.err.println("Lỗi: Phiên " + auctionId + " không ở trạng thái FINISHED (status=" + currentStatus + ")");
                            conn.rollback();
                            conn.setAutoCommit(true);
                            return false; 
                        }

                        amount = rs.getDouble("current_max_price");
                        bidderId = rs.getInt("highest_bidder_id");
                        sellerId = rs.getInt("seller_id");
                        
                        // Kiểm tra dữ liệu hợp lệ
                        if (amount <= 0 || bidderId <= 0 || sellerId <= 0) {
                            System.err.println("Lỗi: Dữ liệu phiên không hợp lệ - amount=" + amount + ", bidderId=" + bidderId + ", sellerId=" + sellerId);
                            conn.rollback();
                            conn.setAutoCommit(true);
                            return false;
                        }
                    } else {
                        System.err.println("Lỗi: Phiên " + auctionId + " không tồn tại");
                        conn.rollback();
                        conn.setAutoCommit(true);
                        return false; // Phiên không tồn tại
                    }
                }
            }

            // 2. Lưu trực tiếp vào bảng payments với trạng thái COMPLETED
            String insertPaymentSql = "INSERT INTO payments (auction_id, from_bidder_id, to_seller_id, amount, status) VALUES (?, ?, ?, ?, 'COMPLETED')";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertPaymentSql)) {
                insertStmt.setInt(1, auctionId);
                insertStmt.setInt(2, bidderId);
                insertStmt.setInt(3, sellerId);
                insertStmt.setDouble(4, amount);
                int rowsInserted = insertStmt.executeUpdate();
                if (rowsInserted == 0) {
                    throw new SQLException("Không thể tạo payment record");
                }
                System.out.println("✓ Tạo payment record: bidder=" + bidderId + ", seller=" + sellerId + ", amount=" + amount);
            }

            // 3. Cộng tiền cho người bán (Seller)
            String addMoneySql = "UPDATE sellers SET account_balance = account_balance + ? WHERE user_id = ?";
            try (PreparedStatement addStmt = conn.prepareStatement(addMoneySql)) {
                addStmt.setDouble(1, amount);
                addStmt.setInt(2, sellerId);
                int rowsUpdated = addStmt.executeUpdate();
                if (rowsUpdated == 0) {
                    throw new SQLException("Người bán (id=" + sellerId + ") không tồn tại trong bảng sellers");
                }
                System.out.println("✓ Cộng tiền cho seller: " + amount + " cho user_id=" + sellerId);
            }

            // 4. Chuyển trạng thái phiên đấu giá sang PAID
            String updateAuctionSql = "UPDATE auctions SET status = 'PAID' WHERE id = ?";
            try (PreparedStatement updateAuctionStmt = conn.prepareStatement(updateAuctionSql)) {
                updateAuctionStmt.setInt(1, auctionId);
                int rowsUpdated = updateAuctionStmt.executeUpdate();
                if (rowsUpdated == 0) {
                    throw new SQLException("Không thể cập nhật trạng thái phiên " + auctionId);
                }
                System.out.println("✓ Cập nhật trạng thái phiên " + auctionId + " thành PAID");
            }

            conn.commit(); // Thành công tất cả thì chốt lưu!
            System.out.println("✓✓✓ TRANSACTION COMMIT THÀNH CÔNG cho phiên " + auctionId);
            return true;

        } catch (SQLException e) {
            System.err.println("Lỗi processPayment: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) try { 
                conn.rollback();
                System.err.println("TRANSACTION ROLLED BACK!");
            } catch (SQLException ex) { 
                ex.printStackTrace();
            }
            return false;
        } finally {
            if (conn != null) try { 
                conn.setAutoCommit(true);
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    // 3. XEM LỊCH SỬ DÒNG TIỀN
    public List<Payment> getPaymentHistoryByUser(int userId) {
        List<Payment> history = new ArrayList<>();
        String sql = "SELECT * FROM payments WHERE from_bidder_id = ? OR to_seller_id = ? ORDER BY payment_date DESC";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    PaymentStatus statusEnum = PaymentStatus.valueOf(rs.getString("status"));
                    history.add(new Payment(
                        rs.getInt("id"),
                        rs.getInt("auction_id"),
                        rs.getInt("from_bidder_id"),
                        rs.getInt("to_seller_id"),
                        rs.getDouble("amount"),
                        statusEnum, 
                        rs.getTimestamp("payment_date").toLocalDateTime()
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getPaymentHistoryByUser: " + e.getMessage());
        }
        return history;
    }

    public boolean confirmPayment(int auctionId, int bidderId) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false); // Bắt đầu Transaction

            // 1. Kiểm tra xem phiên đấu giá đã FINISHED chưa và lấy thông tin tiền, người bán
            String checkAuctionSql = "SELECT current_max_price, seller_id FROM auctions WHERE id = ? AND highest_bidder_id = ? AND status = 'FINISHED' FOR UPDATE";
            double amount = 0;
            int sellerId = 0;

            try (PreparedStatement checkStmt = conn.prepareStatement(checkAuctionSql)) {
                checkStmt.setInt(1, auctionId);
                checkStmt.setInt(2, bidderId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        amount = rs.getDouble("current_max_price");
                        sellerId = rs.getInt("seller_id");
                    } else {
                        // Không tìm thấy, hoặc trạng thái không đúng, hoặc không phải người thắng
                        conn.rollback();
                        return false; 
                    }
                }
            }

            // 2. Tạo hóa đơn thanh toán (Trạng thái COMPLETED luôn)
            String insertPaymentSql = "INSERT INTO payments (auction_id, from_bidder_id, to_seller_id, amount, status) VALUES (?, ?, ?, ?, 'COMPLETED')";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertPaymentSql)) {
                insertStmt.setInt(1, auctionId);
                insertStmt.setInt(2, bidderId);
                insertStmt.setInt(3, sellerId);
                insertStmt.setDouble(4, amount);
                insertStmt.executeUpdate();
            }

            // 3. Cộng tiền vào ví của người bán
            String updateSellerSql = "UPDATE sellers SET account_balance = account_balance + ? WHERE user_id = ?";
            try (PreparedStatement updateSellerStmt = conn.prepareStatement(updateSellerSql)) {
                updateSellerStmt.setDouble(1, amount);
                updateSellerStmt.setInt(2, sellerId);
                updateSellerStmt.executeUpdate();
            }

            // 4. Khóa sổ phiên đấu giá (Chuyển sang PAID)
            String updateAuctionSql = "UPDATE auctions SET status = 'PAID' WHERE id = ?";
            try (PreparedStatement updateAuctionStmt = conn.prepareStatement(updateAuctionSql)) {
                updateAuctionStmt.setInt(1, auctionId);
                updateAuctionStmt.executeUpdate();
            }

            // 5. Mọi thứ thành công -> Commit!
            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Lỗi confirmPayment: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }
}