package com.auction.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * PasswordChangeAPI - Class xử lý API Đổi mật khẩu
 * Chức năng: Xác thực mật khẩu cũ, kiểm tra mật khẩu mới, cập nhật vào database
 * Trả về: Kết quả (thành công/thất bại) kèm thông báo
 */
public class PasswordChangeAPI {
    
    private UserDAO userDAO;
    private DatabaseConnection dbConnection;
    
    // Constructor
    public PasswordChangeAPI() {
        this.userDAO = new UserDAO();
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    /**
     * Phương thức đổi mật khẩu (xác thực mật khẩu cũ)
     * @param email - Email người dùng
     * @param oldPassword - Mật khẩu cũ
     * @param newPassword - Mật khẩu mới
     * @return Map chứa kết quả:
     *         - success (true/false)
     *         - message (thông báo chi tiết)
     */
    public Map<String, Object> changePassword(String email, String oldPassword, String newPassword) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // ========================================
            // 1. KIỂM TRA DỮ LIỆU ĐẦU VÀO
            // ========================================
            
            if (email == null || email.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Vui lòng nhập email!");
                return response;
            }
            
            if (oldPassword == null || oldPassword.isEmpty()) {
                response.put("success", false);
                response.put("message", "Vui lòng nhập mật khẩu cũ!");
                return response;
            }
            
            String validationError = validateNewPassword(newPassword);
            if (validationError != null) {
                response.put("success", false);
                response.put("message", validationError);
                return response;
            }
            
            // Kiểm tra mật khẩu mới có giống mật khẩu cũ không
            if (oldPassword.equals(newPassword)) {
                response.put("success", false);
                response.put("message", "Mật khẩu mới phải khác mật khẩu cũ!");
                return response;
            }
            
            // ========================================
            // 2. KIỂM TRA MẬT KHẨU CŨ CÓ CHÍNH XÁC KHÔNG
            // ========================================
            
            if (!verifyPassword(email, oldPassword)) {
                response.put("success", false);
                response.put("message", "Mật khẩu cũ không chính xác!");
                System.out.println("✗ [Đổi MK thất bại] Email: " + email + " - Mật khẩu cũ sai");
                return response;
            }
            
            // ========================================
            // 3. CẬP NHẬT MẬT KHẨU MỚI
            // ========================================
            
            boolean isUpdated = userDAO.updatePassword(email, newPassword);
            
            if (isUpdated) {
                response.put("success", true);
                response.put("message", "Đổi mật khẩu thành công!");
                
                System.out.println("✓ [Đổi MK thành công] Email: " + email);
            } else {
                response.put("success", false);
                response.put("message", "Lỗi khi cập nhật mật khẩu. Vui lòng thử lại!");
                System.out.println("✗ [Đổi MK thất bại - DB Error] Email: " + email);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi không xác định: " + e.getMessage());
            System.err.println("✗ [Unexpected Error] " + e.getMessage());
            e.printStackTrace();
        }
        
        return response;
    }
    
    /**
     * Phương thức đổi mật khẩu (không cần xác thực mật khẩu cũ - Dùng cho Admin reset password)
     * @param email - Email người dùng
     * @param newPassword - Mật khẩu mới
     * @return Map chứa kết quả
     */
    public Map<String, Object> resetPassword(String email, String newPassword) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Kiểm tra email
            if (email == null || email.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Vui lòng nhập email!");
                return response;
            }
            
            // Kiểm tra mật khẩu mới
            String validationError = validateNewPassword(newPassword);
            if (validationError != null) {
                response.put("success", false);
                response.put("message", validationError);
                return response;
            }
            
            // Kiểm tra email có tồn tại không
            if (!isEmailExists(email)) {
                response.put("success", false);
                response.put("message", "Email không tồn tại trong hệ thống!");
                return response;
            }
            
            // Cập nhật mật khẩu
            boolean isUpdated = userDAO.updatePassword(email, newPassword);
            
            if (isUpdated) {
                response.put("success", true);
                response.put("message", "Reset mật khẩu thành công!");
                System.out.println("✓ [Reset MK thành công] Email: " + email);
            } else {
                response.put("success", false);
                response.put("message", "Lỗi khi cập nhật mật khẩu. Vui lòng thử lại!");
                System.out.println("✗ [Reset MK thất bại] Email: " + email);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi không xác định: " + e.getMessage());
            System.err.println("✗ [Unexpected Error] " + e.getMessage());
            e.printStackTrace();
        }
        
        return response;
    }
    
    /**
     * Kiểm tra mật khẩu mới hợp lệ
     */
    private String validateNewPassword(String password) {
        if (password == null || password.isEmpty()) {
            return "Vui lòng nhập mật khẩu mới!";
        }
        
        if (password.length() < 6) {
            return "Mật khẩu phải tối thiểu 6 ký tự!";
        }
        
        if (password.length() > 100) {
            return "Mật khẩu không được vượt quá 100 ký tự!";
        }
        
        return null;
    }
    
    /**
     * Xác thực mật khẩu (kiểm tra mật khẩu cũ)
     */
    private boolean verifyPassword(String email, String password) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            
            if (conn == null) {
                return false;
            }
            
            String sql = "SELECT password FROM users WHERE email = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, email.trim());
            
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                // So sánh mật khẩu (Lưu ý: Trong thực tế nên dùng hashing)
                return storedPassword.equals(password);
            }
            
            return false;
            
        } catch (SQLException e) {
            System.err.println("✗ [SQL Error] " + e.getMessage());
            e.printStackTrace();
            return false;
            
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("✗ [Lỗi đóng tài nguyên] " + e.getMessage());
            }
        }
    }
    
    /**
     * Kiểm tra email có tồn tại không
     */
    private boolean isEmailExists(String email) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            
            if (conn == null) {
                return false;
            }
            
            String sql = "SELECT id FROM users WHERE email = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, email.trim());
            
            rs = pstmt.executeQuery();
            
            return rs.next();
            
        } catch (SQLException e) {
            System.err.println("✗ [SQL Error] " + e.getMessage());
            e.printStackTrace();
            return false;
            
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("✗ [Lỗi đóng tài nguyên] " + e.getMessage());
            }
        }
    }
    
    // ============================================
    // MAIN: Chạy thử để kiểm tra
    // ============================================
    public static void main(String[] args) {
        PasswordChangeAPI passwordAPI = new PasswordChangeAPI();
        
        // Giả sử có tài khoản test với email: test@gmail.com, password: 123456
        String testEmail = "test@gmail.com";
        
        // Test 1: Đổi mật khẩu thành công
        System.out.println("\n=== Test 1: Đổi mật khẩu thành công ===");
        Map<String, Object> result = passwordAPI.changePassword(testEmail, "123456", "newpass123");
        System.out.println("Kết quả: " + result);
        
        // Test 2: Mật khẩu cũ sai
        System.out.println("\n=== Test 2: Mật khẩu cũ sai ===");
        result = passwordAPI.changePassword(testEmail, "wrongpassword", "newpass456");
        System.out.println("Kết quả: " + result);
        
        // Test 3: Mật khẩu mới giống mật khẩu cũ
        System.out.println("\n=== Test 3: Mật khẩu mới giống mật khẩu cũ ===");
        result = passwordAPI.changePassword(testEmail, "123456", "123456");
        System.out.println("Kết quả: " + result);
        
        // Test 4: Mật khẩu mới quá ngắn
        System.out.println("\n=== Test 4: Mật khẩu mới quá ngắn ===");
        result = passwordAPI.changePassword(testEmail, "123456", "123");
        System.out.println("Kết quả: " + result);
        
        // Test 5: Reset mật khẩu (Admin dùng)
        System.out.println("\n=== Test 5: Reset mật khẩu (Admin) ===");
        result = passwordAPI.resetPassword(testEmail, "resetpass123");
        System.out.println("Kết quả: " + result);
        
        // Test 6: Reset với email không tồn tại
        System.out.println("\n=== Test 6: Reset với email không tồn tại ===");
        result = passwordAPI.resetPassword("nonexistent@gmail.com", "newpass789");
        System.out.println("Kết quả: " + result);
    }
}

