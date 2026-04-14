package com.auction.server.dao;

import com.auction.server.models.Payment;
import com.auction.server.models.PaymentStatus;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO {

    // tao hoa don thanh toan moi khi co nguoi thang cuoc dau gia
    public boolean createPaymentInvoice(int itemId, int fromBidder, int toSeller, double amount) {
        String sql = "INSERT INTO payments (item_id, from_bidder_id, to_seller_id, amount, status) VALUES (?, ?, ?, ?, 'PENDING')";
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            pstmt.setInt(2, fromBidder);
            pstmt.setInt(3, toSeller);
            pstmt.setDouble(4, amount);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 2. XỬ LÝ THANH TOÁN (Chốt đơn, Chuyển tiền cho Seller)
    
    public boolean processPayment(int paymentId) {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try {
            // Bắt đầu Transaction an toàn
            conn.setAutoCommit(false);

            // Bước A: Đọc thông tin hóa đơn xem có đúng là PENDING không
            String checkSql = "SELECT amount, to_seller_id, item_id FROM payments WHERE id = ? AND status = 'PENDING' FOR UPDATE";
            double amount = 0;
            int sellerId = 0;
            int itemId = 0;

            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, paymentId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        amount = rs.getDouble("amount");
                        sellerId = rs.getInt("to_seller_id");
                        itemId = rs.getInt("item_id");
                    } else {
                        conn.rollback(); return false; // Hóa đơn không tồn tại hoặc đã thanh toán rồi
                    }
                }
            }

            // Bước B: Cộng tiền vào ví của Seller (Tiền của Bidder đã bị trừ lúc đặt giá rồi)
            String addMoneySql = "UPDATE sellers SET account_balance = account_balance + ? WHERE user_id = ?";
            try (PreparedStatement addStmt = conn.prepareStatement(addMoneySql)) {
                addStmt.setDouble(1, amount);
                addStmt.setInt(2, sellerId);
                addStmt.executeUpdate();
            }

            // Bước C: Chuyển trạng thái hóa đơn thành COMPLETED
            String updatePaymentSql = "UPDATE payments SET status = 'COMPLETED' WHERE id = ?";
            try (PreparedStatement updatePayStmt = conn.prepareStatement(updatePaymentSql)) {
                updatePayStmt.setInt(1, paymentId);
                updatePayStmt.executeUpdate();
            }

            // Bước D: Chuyển trạng thái Item thành PAID
            String updateItemSql = "UPDATE items SET status = 'PAID' WHERE id = ?";
            try (PreparedStatement updateItemStmt = conn.prepareStatement(updateItemSql)) {
                updateItemStmt.setInt(1, itemId);
                updateItemStmt.executeUpdate();
            }

            // Hoàn tất mọi thứ!
            conn.commit();
            return true;

        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) {}
            return false;
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException e) {}
        }
    }

    // 3. XEM LỊCH SỬ THU/CHI TIỀN (Dành cho Dashboard)
    public List<Payment> getPaymentHistoryByUser(int userId) {
        List<Payment> history = new ArrayList<>();
        String sql = "SELECT * FROM payments WHERE from_bidder_id = ? OR to_seller_id = ? ORDER BY payment_date DESC";
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Lấy chuỗi String từ DB (ví dụ: "COMPLETED")
                    String dbStatus = rs.getString("status");
                    
                    // CHUYỂN ĐỔI: Biến chuỗi String thành hằng số Enum tương ứng
                    PaymentStatus statusEnum = PaymentStatus.valueOf(dbStatus);

                    history.add(new Payment(
                        rs.getInt("id"),
                        rs.getInt("item_id"),
                        rs.getInt("from_bidder_id"),
                        rs.getInt("to_seller_id"),
                        rs.getDouble("amount"),
                        statusEnum, // Truyền Enum vào thay vì String
                        rs.getTimestamp("payment_date").toLocalDateTime()
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }
}