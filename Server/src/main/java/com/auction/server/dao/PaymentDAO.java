package com.auction.server.dao;

import com.auction.server.models.Payment;
import com.auction.server.models.PaymentStatus;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO {

    // 1. TẠO HÓA ĐƠN (Gắn với Phiên đấu giá - auctionId)
    public boolean createPaymentInvoice(int auctionId, int fromBidder, int toSeller, double amount) {
        String sql = "INSERT INTO payments (auction_id, from_bidder_id, to_seller_id, amount, status) VALUES (?, ?, ?, ?, 'PENDING')";
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, auctionId);
            pstmt.setInt(2, fromBidder);
            pstmt.setInt(3, toSeller);
            pstmt.setDouble(4, amount);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 2. XỬ LÝ THANH TOÁN (Transaction hoàn hảo)
    public boolean processPayment(int paymentId) {
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try {
            conn.setAutoCommit(false);

            // Bước A: Khóa dòng hóa đơn để kiểm tra (Tránh bị bấm thanh toán 2 lần)
            String checkSql = "SELECT amount, to_seller_id, auction_id FROM payments WHERE id = ? AND status = 'PENDING' FOR UPDATE";
            double amount = 0;
            int sellerId = 0;
            int auctionId = 0;

            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, paymentId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        amount = rs.getDouble("amount");
                        sellerId = rs.getInt("to_seller_id");
                        auctionId = rs.getInt("auction_id"); // Lấy mã phiên đấu giá
                    } else {
                        conn.rollback(); return false; 
                    }
                }
            }

            // Bước B: Cộng tiền cho người bán 
            String addMoneySql = "UPDATE sellers SET account_balance = account_balance + ? WHERE user_id = ?";
            try (PreparedStatement addStmt = conn.prepareStatement(addMoneySql)) {
                addStmt.setDouble(1, amount);
                addStmt.setInt(2, sellerId);
                addStmt.executeUpdate();
            }

            // Bước C: Chốt hóa đơn
            String updatePaymentSql = "UPDATE payments SET status = 'COMPLETED' WHERE id = ?";
            try (PreparedStatement updatePayStmt = conn.prepareStatement(updatePaymentSql)) {
                updatePayStmt.setInt(1, paymentId);
                updatePayStmt.executeUpdate();
            }

            // Bước D: Đổi trạng thái PHIÊN ĐẤU GIÁ (auctions) thành PAID
            String updateAuctionSql = "UPDATE auctions SET status = 'PAID' WHERE id = ?";
            try (PreparedStatement updateAuctionStmt = conn.prepareStatement(updateAuctionSql)) {
                updateAuctionStmt.setInt(1, auctionId);
                updateAuctionStmt.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) {}
            return false;
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException e) {}
        }
    }

    // 3. XEM LỊCH SỬ DÒNG TIỀN
    public List<Payment> getPaymentHistoryByUser(int userId) {
        List<Payment> history = new ArrayList<>();
        String sql = "SELECT * FROM payments WHERE from_bidder_id = ? OR to_seller_id = ? ORDER BY payment_date DESC";
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    PaymentStatus statusEnum = PaymentStatus.valueOf(rs.getString("status"));

                    history.add(new Payment(
                        rs.getInt("id"),
                        rs.getInt("auction_id"), // Đã sửa thành auction_id
                        rs.getInt("from_bidder_id"),
                        rs.getInt("to_seller_id"),
                        rs.getDouble("amount"),
                        statusEnum, 
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