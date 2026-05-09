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

    public Map<String, Object> loginUser(String email, String plainPassword) {
        // THÊM isActive vào SELECT để kiểm tra
        String sql = "SELECT id, password, role, isActive FROM users WHERE email = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String hashedPassword = rs.getString("password");
                    if (BCrypt.checkpw(plainPassword, hashedPassword)) {
                        
                        // ✅ THÊM: Kiểm tra tài khoản có bị khóa không
                        boolean isActive = rs.getBoolean("isActive");
                        if (!isActive) {
                            // Trả về null đặc biệt để báo bị khóa
                            Map<String, Object> banned = new HashMap<>();
                            banned.put("banned", true);
                            return banned;
                        }
                        
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
        // Dùng LEFT JOIN để móc dữ liệu từ bảng sellers và bidders dựa vào user_id
        String sql = "SELECT u.id, u.username, u.email, u.role, " +
                     "s.total_rating, s.sale_count, s.account_balance AS seller_balance, " +
                     "b.account_balance AS bidder_balance " +
                     "FROM users u " +
                     "LEFT JOIN sellers s ON u.id = s.user_id " +
                     "LEFT JOIN bidders b ON u.id = b.user_id " +
                     "WHERE u.id = ?";
                     
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
             
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    UserDTO user = new UserDTO();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    String role = rs.getString("role");
                    user.setRole(role);
                    
                    // Phân loại Role để lấy đúng dữ liệu từ bảng con tương ứng
                    if ("SELLER".equalsIgnoreCase(role)) {
                        user.setBalance(rs.getDouble("seller_balance"));
                        user.setTotalRating(rs.getDouble("total_rating"));
                        user.setSaleCount(rs.getInt("sale_count"));
                    } else if ("BIDDER".equalsIgnoreCase(role)) {
                        user.setBalance(rs.getDouble("bidder_balance"));
                    }
                    
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public List<UserDTO> getAllUsers() {
        List<UserDTO> users = new ArrayList<>();
        // THÊM isActive vào câu truy vấn
        String sql = "SELECT id, username, email, role, isActive FROM users ORDER BY id";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                UserDTO user = new UserDTO();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setRole(rs.getString("role"));
                user.setActive(rs.getBoolean("isActive")); // ✅ THÊM
                users.add(user);
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