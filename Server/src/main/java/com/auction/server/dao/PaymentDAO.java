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
    public boolean processPayment(int paymentId) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            
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
                        auctionId = rs.getInt("auction_id");
                    } else {
                        conn.rollback();
                        return false; 
                    }
                }
            }

            String addMoneySql = "UPDATE sellers SET account_balance = account_balance + ? WHERE user_id = ?";
            try (PreparedStatement addStmt = conn.prepareStatement(addMoneySql)) {
                addStmt.setDouble(1, amount);
                addStmt.setInt(2, sellerId);
                addStmt.executeUpdate();
            }

            String updatePaymentSql = "UPDATE payments SET status = 'COMPLETED' WHERE id = ?";
            try (PreparedStatement updatePayStmt = conn.prepareStatement(updatePaymentSql)) {
                updatePayStmt.setInt(1, paymentId);
                updatePayStmt.executeUpdate();
            }

            String updateAuctionSql = "UPDATE auctions SET status = 'PAID' WHERE id = ?";
            try (PreparedStatement updateAuctionStmt = conn.prepareStatement(updateAuctionSql)) {
                updateAuctionStmt.setInt(1, auctionId);
                updateAuctionStmt.executeUpdate();
            }

            conn.commit();
            return true;
            
        } catch (SQLException e) {
            System.err.println("Lỗi processPayment: " + e.getMessage());
            return false;
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
}