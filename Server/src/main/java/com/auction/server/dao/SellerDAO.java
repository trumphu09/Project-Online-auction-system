package com.auction.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.auction.server.models.Seller;
import com.auction.server.models.SellerDTO;

public class SellerDAO {
    private UserDAO userDAO = new UserDAO();

    public boolean registerSeller(Seller seller) {
        boolean isUserCreated = userDAO.registerUser(seller, "SELLER");
        if (isUserCreated) {
            String sql = "INSERT INTO sellers (user_id, account_balance) VALUES (?, 0.0)";
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setInt(1, seller.getId());
                return pstmt.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("Lỗi registerSeller: " + e.getMessage());
                return false;
            }
        }
        return false;
    }

    public boolean registerSeller(String username, String password, String email) {
        Seller tempSeller = new Seller(0, username, password, email);
        return registerSeller(tempSeller);
    }

    public SellerDTO getSellerById(int sellerId){
        String sql = "SELECT u.id, u.username, u.email, s.account_balance, s.total_rating, s.sale_count "+
                     "FROM users u JOIN sellers s ON u.id = s.user_id WHERE u.id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setInt(1, sellerId);
            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new SellerDTO(
                        rs.getInt("id"), 
                        rs.getString("username"), 
                        rs.getString("email"), 
                        rs.getDouble("account_balance"), 
                        rs.getDouble("total_rating"), 
                        rs.getInt("sale_count")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getSellerById: " + e.getMessage());
        }
        return null;
    }
    
    public boolean rateSeller(int sellerId, double newScore) {
        if (newScore < 0.0) newScore = 0.0;
        if (newScore > 5.0) newScore = 5.0;
        
        String sql = "UPDATE sellers " +
                     "SET total_rating = ((total_rating * sale_count) + ?) / (sale_count + 1), " +
                     "sale_count = sale_count + 1 " +
                     "WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDouble(1, newScore);
            pstmt.setInt(2, sellerId);
            return pstmt.executeUpdate() > 0; 
            
        } catch (SQLException e) {
            System.err.println("Lỗi rateSeller: " + e.getMessage());
            return false;
        }
    }
}