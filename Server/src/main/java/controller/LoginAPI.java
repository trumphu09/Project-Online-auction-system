package controller;

import com.auction.server.dao.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


public class LoginAPI {
    
    private DatabaseConnection dbConnection;
    
    // Constructor: Khởi tạo kết nối database
    public LoginAPI() {
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    /**
     * Phương thức đăng nhập
     * @param username - Tên đăng nhập
     * @param password - Mật khẩu
     * @return Map chứa kết quả: 
     *         - success (true/false)
     *         - message (thông báo)
     *         - userId, username, email, role (nếu thành công)
     */
    public Map<String, Object> login(String username, String password) {
        Map<String, Object> response = new HashMap<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            // 1. Kiểm tra dữ liệu đầu vào
            if (username == null || username.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Vui lòng nhập username!");
                return response;
            }
            
            if (password == null || password.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Vui lòng nhập mật khẩu!");
                return response;
            }
            
            // 2. Lấy kết nối từ DatabaseConnection
            conn = dbConnection.getConnection();
            
            if (conn == null) {
                response.put("success", false);
                response.put("message", "Lỗi kết nối database!");
                return response;
            }
            
            // 3. Truy vấn bảng users
            String sql = "SELECT id, username, email, password, role FROM users WHERE username = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username.trim());
            
            rs = pstmt.executeQuery();
            
            // 4. Kiểm tra kết quả
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                
                // Kiểm tra mật khẩu (đơn giản: so sánh trực tiếp)
                // Lưu ý: Trong thực tế nên sử dụng hashing (BCrypt, SHA256, v.v.)
                if (storedPassword.equals(password)) {
                    // ✓ Đăng nhập thành công
                    response.put("success", true);
                    response.put("message", "Đăng nhập thành công!");
                    response.put("userId", rs.getInt("id"));
                    response.put("username", rs.getString("username"));
                    response.put("email", rs.getString("email"));
                    response.put("role", rs.getString("role"));
                    
                    System.out.println("✓ [Login thành công] User: " + username);
                } else {
                    // ✗ Mật khẩu sai
                    response.put("success", false);
                    response.put("message", "Mật khẩu không chính xác!");
                    System.out.println("✗ [Login thất bại] Sai mật khẩu: " + username);
                }
            } else {
                // ✗ Không tìm thấy user
                response.put("success", false);
                response.put("message", "Tên đăng nhập không tồn tại!");
                System.out.println("✗ [Login thất bại] User không tồn tại: " + username);
            }
            
        } catch (SQLException e) {
            // Xử lý lỗi SQL
            response.put("success", false);
            response.put("message", "Lỗi cơ sở dữ liệu: " + e.getMessage());
            System.err.println("✗ [SQL Error] " + e.getMessage());
            e.printStackTrace();
            
        } catch (Exception e) {
            // Xử lý lỗi chung
            response.put("success", false);
            response.put("message", "Lỗi không xác định: " + e.getMessage());
            System.err.println("✗ [Unexpected Error] " + e.getMessage());
            e.printStackTrace();
            
        } finally {
            // Đóng tài nguyên
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                // Không đóng conn vì nó được quản lý bởi DatabaseConnection (Singleton)
            } catch (SQLException e) {
                System.err.println("✗ [Lỗi đóng tài nguyên] " + e.getMessage());
            }
        }
        
        return response;
    }
    
    /**
     * Phương thức kiểm tra username có tồn tại không
     * @param username - Tên đăng nhập
     * @return true nếu tồn tại, false nếu không tồn tại
     */
    public boolean isUsernameExists(String username) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            if (username == null || username.trim().isEmpty()) {
                return false;
            }
            
            conn = dbConnection.getConnection();
            
            if (conn == null) {
                return false;
            }
            
            String sql = "SELECT id FROM users WHERE username = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username.trim());
            
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
        LoginAPI loginAPI = new LoginAPI();
        
        // Test 1: Đăng nhập thành công (nếu user này tồn tại)
        System.out.println("\n=== Test 1: Đăng nhập ===");
        Map<String, Object> result = loginAPI.login("testuser", "password123");
        System.out.println("Kết quả: " + result);
        
        // Test 2: Sai mật khẩu
        System.out.println("\n=== Test 2: Sai mật khẩu ===");
        result = loginAPI.login("testuser", "wrongpassword");
        System.out.println("Kết quả: " + result);
        
        // Test 3: User không tồn tại
        System.out.println("\n=== Test 3: User không tồn tại ===");
        result = loginAPI.login("nonexistent", "password123");
        System.out.println("Kết quả: " + result);
        
        // Test 4: Kiểm tra username tồn tại
        System.out.println("\n=== Test 4: Kiểm tra username tồn tại ===");
        boolean exists = loginAPI.isUsernameExists("testuser");
        System.out.println("Username tồn tại: " + exists);
    }
}

