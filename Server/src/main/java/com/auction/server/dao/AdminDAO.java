package com.auction.server.dao;

import com.auction.server.models.BidderDTO;
import com.auction.server.models.SellerDTO;
import com.auction.server.models.AdminDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminDAO {
    // Lấy danh sách tất cả người dùng
    public List<Object> getAllUsers() {   
        List<Object> users = new ArrayList<>();
        String sql = "SELECT id, username, email, role FROM users";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String username = rs.getString("username");
                String email = rs.getString("email");
                String role = rs.getString("role");

                Object user = null;
                if ("BIDDER".equalsIgnoreCase(role)) {
                    user = new BidderDTO(id, username, email, 0.0);
                } else if ("SELLER".equalsIgnoreCase(role)) {
                    user = new SellerDTO(id, username, email, 0.0, 5.0, 0);
                } else if ("ADMIN".equalsIgnoreCase(role)) {
                    user = new AdminDTO(id, username, email, "ADMIN");
                }
                
                if (user != null) {
                    users.add(user);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getAllUsers: " + e.getMessage());
        }
        return users;
    }

    // Xóa người dùng theo ID
    public boolean deleteUser(int userId, String role) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            
            String deleteSubTableSql = role.equalsIgnoreCase("BIDDER") ? 
                                       "DELETE FROM bidders WHERE user_id = ?" : 
                                       "DELETE FROM sellers WHERE user_id = ?";
                                       
            try (PreparedStatement pstmtSub = conn.prepareStatement(deleteSubTableSql)) {
                pstmtSub.setInt(1, userId);
                pstmtSub.executeUpdate();
            }

            String deleteUserSql = "DELETE FROM users WHERE id = ?";
            try (PreparedStatement pstmtUser = conn.prepareStatement(deleteUserSql)) {
                pstmtUser.setInt(1, userId);
                int affectedRows = pstmtUser.executeUpdate();
                
                if (affectedRows > 0) {
                    conn.commit();
                    return true;
                }
            }
            conn.rollback();
            
        } catch (SQLException e) {
            System.err.println("Lỗi deleteUser: " + e.getMessage());
        }
        return false;
    }

    // Khóa hoặc mở tài khoản người dùng
    public boolean setUserStatus(int userId, boolean status) {
        String sql = "UPDATE users SET isActive = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setBoolean(1, status);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Lỗi setUserStatus: " + e.getMessage());
            return false;
        }
    }
}