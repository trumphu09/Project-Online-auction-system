package com.auction.server.dao;

import com.auction.server.models.User;
import com.auction.server.models.BidderDTO;
import com.auction.server.models.SellerDTO;
import com.auction.server.models.AdminDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminDAO {
    // lay danh sach tat ca nguoi dung
    public List<Object> getAllUsers() {   
    List<Object> users = new ArrayList<>();
    String sql = "SELECT id, username, email, role FROM users";

    Connection conn = DatabaseConnection.getInstance().getConnection();
    try (Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {

        while (rs.next()) {
            int id = rs.getInt("id");
            String username = rs.getString("username");
            String email = rs.getString("email");
            String role = rs.getString("role");

            Object user = null;
            if ("BIDDER".equalsIgnoreCase(role)) {
                user = new BidderDTO(id, username, email, 0.0);  // DTO
            } else if ("SELLER".equalsIgnoreCase(role)) {
                user = new SellerDTO(id, username, email, 0.0, 5.0, 0);  // DTO
            } else if ("ADMIN".equalsIgnoreCase(role)) {
                user = new AdminDTO(id, username, email, "ADMIN");  // DTO
            }
            
            if (user != null) {
                users.add(user);
            }
        }
    } catch (SQLException e) {
            e.printStackTrace();
    } finally {
        try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
        } catch (SQLException e) {
                e.printStackTrace();
            }
    }
    return users;
    }

    // xoa nguoi dung theo id
    public boolean deleteUser(int userId, String role) {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        
        try {
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
            try { conn.rollback(); } catch (SQLException ex) {}
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException e) {}
        }
        return false;
    }

    // khoa hoac mo tai khoan nguoi dung
    public boolean setUserStatus(int userId, boolean status) {
        String sql = "UPDATE users SET isActive = ? WHERE id = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, status);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }
}
