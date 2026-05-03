package com.auction.server.dao;

import com.auction.server.models.UserDTO;
import com.auction.server.models.UserRole;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement; // Nhớ thêm import này
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserDAO {

    // === ĐÃ SỬA: Áp dụng chuẩn OCP và Transaction ===
    public boolean registerUser(String username, String password, String email, UserRole role) {
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        String sqlUser = "INSERT INTO users (username, password, email, role) VALUES (?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false); // 1. Mở Transaction (Chống rác DB)

            int generatedUserId = -1;
            
            // 2. Thêm vào bảng cha (users) và lấy ID vừa tạo
            try (PreparedStatement pstmt = conn.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, username);
                pstmt.setString(2, hashedPassword);
                pstmt.setString(3, email);
                pstmt.setString(4, role.name());
                
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Tạo user thất bại.");
                }

                // Lấy ID tự tăng từ MySQL
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        generatedUserId = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Không lấy được ID của user vừa tạo.");
                    }
                }
            }

            // 3. TÍNH ĐA HÌNH (OCP): Gọi Factory để tự động thêm vào bảng con tương ứng
            // Chỉ thêm bảng con nếu không phải ADMIN
            if (role != UserRole.ADMIN && role != UserRole.INACTIVE) {
                if (role == UserRole.BIDDER) {
                    String sqlBidder = "INSERT INTO bidders (user_id, account_balance) VALUES (?, 0)";
                    try (PreparedStatement pstmtBidder = conn.prepareStatement(sqlBidder)) {
                        pstmtBidder.setInt(1, generatedUserId);
                        pstmtBidder.executeUpdate();
                    }
                } else if (role == UserRole.SELLER) {
                    String sqlSeller = "INSERT INTO sellers (user_id, total_rating, sale_count, account_balance) VALUES (?, 0, 0, 0)";
                    try (PreparedStatement pstmtSeller = conn.prepareStatement(sqlSeller)) {
                        pstmtSeller.setInt(1, generatedUserId);
                        pstmtSeller.executeUpdate();
                    }
                }
            }

            conn.commit(); // 4. Chốt hạ, ghi tất cả vào Database
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback(); // Có lỗi thì hoàn tác sạch sẽ
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Trả lại ống nước về trạng thái mặc định
                    conn.close(); // Quan trọng: Đóng connection để trả về Hikari Pool
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    // ... (Giữ nguyên các hàm loginUser, getUserById, getAllUsers... ở bên dưới) ...
    public Map<String, Object> loginUser(String email, String plainPassword) {
        String sql = "SELECT id, password, role FROM users WHERE email = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String hashedPassword = rs.getString("password");
                    if (BCrypt.checkpw(plainPassword, hashedPassword)) {
                        Map<String, Object> userDetails = new HashMap<>();
                        userDetails.put("userId", rs.getInt("id"));
                        userDetails.put("role", UserRole.fromString(rs.getString("role")));
                        return userDetails;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public boolean updateUserRole(int userId, UserRole newRole) {
        String sql = "UPDATE users SET role = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newRole.name());
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public UserDTO getUserById(int userId) {
        String sql = "SELECT id, username, email, role FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new UserDTO(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        UserRole.fromString(rs.getString("role"))
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<UserDTO> getAllUsers() {
        List<UserDTO> users = new ArrayList<>();
        String sql = "SELECT id, username, email, role FROM users ORDER BY id";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                users.add(new UserDTO(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("email"),
                    UserRole.fromString(rs.getString("role"))
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        String sqlSelect = "SELECT password FROM users WHERE id = ?";
        String sqlUpdate = "UPDATE users SET password = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            String hashedPassword = null;
            try (PreparedStatement selectStmt = conn.prepareStatement(sqlSelect)) {
                selectStmt.setInt(1, userId);
                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (rs.next()) {
                        hashedPassword = rs.getString("password");
                    }
                }
            }

            if (hashedPassword != null && BCrypt.checkpw(oldPassword, hashedPassword)) {
                String newHashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
                try (PreparedStatement updateStmt = conn.prepareStatement(sqlUpdate)) {
                    updateStmt.setString(1, newHashedPassword);
                    updateStmt.setInt(2, userId);
                    return updateStmt.executeUpdate() > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}