package controller;

import com.auction.server.dao.DatabaseConnection;
import com.auction.server.dao.UserDAO;
import com.auction.server.models.User;
import com.auction.server.models.Bidder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * RegisterAPI - Class xử lý API Đăng ký tài khoản
 * Chức năng: Kiểm tra dữ liệu, tạo tài khoản mới, lưu vào database
 * Trả về: Kết quả (thành công/thất bại) kèm thông báo
 */
public class RegisterAPI {
    
    private UserDAO userDAO;
    private DatabaseConnection dbConnection;
    
    // Regex kiểm tra email
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
    
    // Constructor
    public RegisterAPI() {
        this.userDAO = new UserDAO();
        this.dbConnection = DatabaseConnection.getInstance();
    }
    
    /**
     * Phương thức đăng ký tài khoản
     * @param username - Tên đăng nhập
     * @param password - Mật khẩu
     * @param email - Email
     * @param role - Vai trò (BIDDER, SELLER, ADMIN)
     * @return Map chứa kết quả:
     *         - success (true/false)
     *         - message (thông báo chi tiết)
     *         - userId (nếu thành công)
     */
    public Map<String, Object> register(String username, String password, String email, String role) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // ========================================
            // 1. KIỂM TRA DỮ LIỆU ĐẦU VÀO
            // ========================================
            
            // Kiểm tra username
            String validationError = validateUsername(username);
            if (validationError != null) {
                response.put("success", false);
                response.put("message", validationError);
                return response;
            }
            
            // Kiểm tra password
            validationError = validatePassword(password);
            if (validationError != null) {
                response.put("success", false);
                response.put("message", validationError);
                return response;
            }
            
            // Kiểm tra email
            validationError = validateEmail(email);
            if (validationError != null) {
                response.put("success", false);
                response.put("message", validationError);
                return response;
            }
            
            // Kiểm tra role
            if (role == null || !isValidRole(role)) {
                response.put("success", false);
                response.put("message", "Vai trò không hợp lệ! Hãy chọn: BIDDER, SELLER, ADMIN");
                return response;
            }
            
            // ========================================
            // 2. KIỂM TRA USERNAME/EMAIL ĐÃ TỒN TẠI CHƯA
            // ========================================
            
            if (isUsernameExists(username)) {
                response.put("success", false);
                response.put("message", "Username '" + username + "' đã tồn tại!");
                return response;
            }
            
            if (isEmailExists(email)) {
                response.put("success", false);
                response.put("message", "Email '" + email + "' đã được đăng ký!");
                return response;
            }
            
            // ========================================
            // 3. ĐĂNG KÝ TÀI KHOẢN
            // ========================================
            
            boolean isRegistered = userDAO.registerUser(username, password, email, role);
            
            if (isRegistered) {
                // Lấy userId vừa tạo
                int userId = getUserIdByUsername(username);
                
                response.put("success", true);
                response.put("message", "Đăng ký tài khoản thành công!");
                response.put("userId", userId);
                response.put("username", username);
                response.put("email", email);
                response.put("role", role);
                
                System.out.println("✓ [Register thành công] User: " + username + " (Role: " + role + ")");
            } else {
                response.put("success", false);
                response.put("message", "Lỗi khi tạo tài khoản. Vui lòng thử lại!");
                System.out.println("✗ [Register thất bại] User: " + username);
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
     * Phương thức đăng ký với object User (OOP Style)
     * @param user - Object User (Bidder, Seller, hoặc Admin)
     * @param role - Vai trò
     * @return Map chứa kết quả
     */
    public Map<String, Object> register(User user, String role) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Kiểm tra object null
            if (user == null) {
                response.put("success", false);
                response.put("message", "Đối tượng User không hợp lệ!");
                return response;
            }
            
            // Kiểm tra dữ liệu từ object
            String validationError = validateUsername(user.getUsername());
            if (validationError != null) {
                response.put("success", false);
                response.put("message", validationError);
                return response;
            }
            
            validationError = validatePassword(user.getPassword());
            if (validationError != null) {
                response.put("success", false);
                response.put("message", validationError);
                return response;
            }
            
            validationError = validateEmail(user.getEmail());
            if (validationError != null) {
                response.put("success", false);
                response.put("message", validationError);
                return response;
            }
            
            // Kiểm tra trùng lặp
            if (isUsernameExists(user.getUsername())) {
                response.put("success", false);
                response.put("message", "Username '" + user.getUsername() + "' đã tồn tại!");
                return response;
            }
            
            if (isEmailExists(user.getEmail())) {
                response.put("success", false);
                response.put("message", "Email '" + user.getEmail() + "' đã được đăng ký!");
                return response;
            }
            
            // Đăng ký
            boolean isRegistered = userDAO.registerUser(user, role);
            
            if (isRegistered) {
                response.put("success", true);
                response.put("message", "Đăng ký tài khoản thành công!");
                response.put("userId", user.getId()); // Đã được cập nhật trong DAO
                response.put("username", user.getUsername());
                response.put("email", user.getEmail());
                response.put("role", role);
                
                System.out.println("✓ [Register Object thành công] User: " + user.getUsername() + " (ID: " + user.getId() + ")");
            } else {
                response.put("success", false);
                response.put("message", "Lỗi khi tạo tài khoản. Vui lòng thử lại!");
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
     * Kiểm tra username hợp lệ
     */
    private String validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return "Vui lòng nhập username!";
        }
        
        username = username.trim();
        
        if (username.length() < 3) {
            return "Username phải tối thiểu 3 ký tự!";
        }
        
        if (username.length() > 50) {
            return "Username không được vượt quá 50 ký tự!";
        }
        
        if (!username.matches("^[a-zA-Z0-9_.-]+$")) {
            return "Username chỉ được chứa chữ, số, dấu gạch dưới và dấu chấm!";
        }
        
        return null;
    }
    
    /**
     * Kiểm tra password hợp lệ
     */
    private String validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return "Vui lòng nhập mật khẩu!";
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
     * Kiểm tra email hợp lệ
     */
    private String validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "Vui lòng nhập email!";
        }
        
        email = email.trim();
        
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return "Email không hợp lệ!";
        }
        
        if (email.length() > 100) {
            return "Email không được vượt quá 100 ký tự!";
        }
        
        return null;
    }
    
    /**
     * Kiểm tra role hợp lệ
     */
    private boolean isValidRole(String role) {
        return role != null && (role.equals("BIDDER") || role.equals("SELLER") || role.equals("ADMIN"));
    }
    
    /**
     * Kiểm tra username đã tồn tại
     */
    private boolean isUsernameExists(String username) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
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
    
    /**
     * Kiểm tra email đã tồn tại
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
    
    /**
     * Lấy userId từ username
     */
    private int getUserIdByUsername(String username) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbConnection.getConnection();
            
            if (conn == null) {
                return -1;
            }
            
            String sql = "SELECT id FROM users WHERE username = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username.trim());
            
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("id");
            }
            
        } catch (SQLException e) {
            System.err.println("✗ [SQL Error] " + e.getMessage());
            e.printStackTrace();
            
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                System.err.println("✗ [Lỗi đóng tài nguyên] " + e.getMessage());
            }
        }
        
        return -1;
    }
    
    // ============================================
    // MAIN: Chạy thử để kiểm tra
    // ============================================
    public static void main(String[] args) {
        RegisterAPI registerAPI = new RegisterAPI();
        
        // Tạo timestamp để tránh lỗi trùng lặp
        long time = System.currentTimeMillis();
        
        // Test 1: Đăng ký thành công
        System.out.println("\n=== Test 1: Đăng ký thành công ===");
        String testUsername = "newuser_" + time;
        String testEmail = "newuser_" + time + "@gmail.com";
        Map<String, Object> result = registerAPI.register(testUsername, "password123", testEmail, "BIDDER");
        System.out.println("Kết quả: " + result);
        
        // Test 2: Username đã tồn tại
        System.out.println("\n=== Test 2: Username đã tồn tại ===");
        result = registerAPI.register(testUsername, "password123", "other_" + time + "@gmail.com", "SELLER");
        System.out.println("Kết quả: " + result);
        
        // Test 3: Email đã tồn tại
        System.out.println("\n=== Test 3: Email đã tồn tại ===");
        result = registerAPI.register("another_" + time, "password123", testEmail, "ADMIN");
        System.out.println("Kết quả: " + result);
        
        // Test 4: Username quá ngắn
        System.out.println("\n=== Test 4: Username quá ngắn ===");
        result = registerAPI.register("ab", "password123", "test4_" + time + "@gmail.com", "BIDDER");
        System.out.println("Kết quả: " + result);
        
        // Test 5: Email không hợp lệ
        System.out.println("\n=== Test 5: Email không hợp lệ ===");
        result = registerAPI.register("testuser5_" + time, "password123", "invalid-email", "SELLER");
        System.out.println("Kết quả: " + result);
        
        // Test 6: Role không hợp lệ
        System.out.println("\n=== Test 6: Role không hợp lệ ===");
        result = registerAPI.register("testuser6_" + time, "password123", "test6_" + time + "@gmail.com", "SUPERUSER");
        System.out.println("Kết quả: " + result);
        
        // Test 7: Mật khẩu quá ngắn
        System.out.println("\n=== Test 7: Mật khẩu quá ngắn ===");
        result = registerAPI.register("testuser7_" + time, "123", "test7_" + time + "@gmail.com", "BIDDER");
        System.out.println("Kết quả: " + result);
        
        // Test 8: Đăng ký với object Bidder
        System.out.println("\n=== Test 8: Đăng ký với object Bidder ===");
        Bidder newBidder = new Bidder(0, "bidder_" + time, "pass1234", "bidder_" + time + "@gmail.com", 5000.0);
        result = registerAPI.register(newBidder, "BIDDER");
        System.out.println("Kết quả: " + result);
    }
}

