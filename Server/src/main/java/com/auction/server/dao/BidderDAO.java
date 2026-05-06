package com.auction.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.auction.server.models.Bidder;
import com.auction.server.models.BidderDTO;

public class BidderDAO {
    private UserDAO userDAO = new UserDAO();

    public BidderDTO getBidderById(int bidderId){
        String sql = "SELECT u.id, u.username, u.email, b.account_balance "+
                     "FROM users u JOIN bidders b ON u.id = b.user_id WHERE u.id = ?";   

        // ĐÃ SỬA
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, bidderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if(rs.next()){
                    int id = rs.getInt("id");
                    String username = rs.getString("username");
                    String email = rs.getString("email");
                    double accountBalance = rs.getDouble("account_balance");
                    return new BidderDTO(id, username, email, accountBalance);
                }
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateBalance(int userid, double amount){
        String sql = "UPDATE bidders SET account_balance = account_balance + ? WHERE user_id = ?";
        
        // ĐÃ SỬA
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDouble(1, amount);
            pstmt.setInt(2, userid);
            int rowsUpdated = pstmt.executeUpdate();
            return rowsUpdated > 0; 
            
        } catch(SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deductBalance(int userId, double amount){
        if(amount < 0){
            amount = Math.abs(amount);
        }
        String sql = "UPDATE bidders SET account_balance = account_balance - ? WHERE user_id = ? AND account_balance > ?";

        // ĐÃ SỬA
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDouble(1, amount);
            pstmt.setInt(2, userId);
            pstmt.setDouble(3, amount);
            int rowsUpdated = pstmt.executeUpdate();
            return rowsUpdated > 0;
            
        } catch(SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}