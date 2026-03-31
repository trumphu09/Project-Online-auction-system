package com.auction.server.dao;

import com.auction.server.models.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import com.auction.server.models.Bidder;

public class UserDAO {

    // add new user to database User
    public boolean registerUser(String username, String password, String email, String role) {
        String sql = "INSERT INTO users (username, password, email, role) VALUES (?, ?, ?, ?)";

        // 1. Lấy đường ống kết nối ra NGOÀI khối try (Tuyệt đối không đóng nó lại)
        Connection conn = DatabaseConnection.getInstance().getConnection();

        // 2. Chỉ cho PreparedStatement vào trong ngoặc để nó tự đóng dọn rác sau khi chạy lệnh
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, email); 
            pstmt.setString(4, role);

            int rowsInserted = pstmt.executeUpdate();
            return rowsInserted > 0; 

        } catch (SQLException e) {
            System.err.println("-> [Loi] Khong the them nguoi dung (" + username + "): " + e.getMessage());
            return false;
        }
    }

    // login method return role if exist
    public String loginUser(String email, String password){
        String sql = "SELECT role FROM users WHERE email = ? AND password = ?";
        
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1, email);
            pstmt.setString(2, password);

            try (java.sql.ResultSet rs = pstmt.executeQuery()){
                if (rs.next()){
                    return rs.getString("role");
                }
            }

        } catch(SQLException e){
            System.err.println("user doesn't exist: " + e.getMessage());
        }
        return null;
    }
    

    // update password for user
    public boolean updatePassword(String email, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE email = ?";
        
        Connection conn = DatabaseConnection.getInstance().getConnection();
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newPassword);
            pstmt.setString(2, email);
            
            int rowsUpdated = pstmt.executeUpdate();
            
            return rowsUpdated > 0; 
            
        } catch (SQLException e) {
            System.err.println("-> [Lỗi] Không thể đổi mật khẩu cho User ID " + email + ": " + e.getMessage());
            return false;
        }
    }

    // tu doi tuong chuyen sang them vao database
    public boolean registerUser(User user, String role) {
        
        // Cấu trúc SQL chuẩn cho bảng users
        String sql = "INSERT INTO users (username, password, email, role) VALUES (?, ?, ?, ?)";
        Connection conn = DatabaseConnection.getInstance().getConnection();

        // Thêm Statement.RETURN_GENERATED_KEYS để lát nữa đòi MySQL nhả cái ID ra
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // 3. MÓC DỮ LIỆU TỪ OBJECT RA ĐỂ NẠP VÀO SQL
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, role);

            int rowsInserted = pstmt.executeUpdate();
            
            if (rowsInserted > 0) {
                // 4. LẤY SỐ THỨ TỰ (ID) TỪ MYSQL VÀ NẠP NGƯỢC LẠI VÀO OBJECT
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int newIdFromDB = generatedKeys.getInt(1); // Máy MySQL vừa nhả ra số ID thật
                        
                        user.setId(newIdFromDB); // Cập nhật số ID này vào cái Object đang nằm trong RAM
                        
                        return true; 
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("-> [Lỗi] Không thể đăng ký User (" + user.getUsername() + "): " + e.getMessage());
        }
        return false;
    }

    // --- HÀM MAIN ĐỂ CHẠY TEST ---
    // --- HÀM MAIN ĐỂ CHẠY TEST LIÊN HOÀN ---
    public static void main(String[] args) {
        UserDAO dao = new UserDAO();
        
        System.out.println("========== BẮT ĐẦU TEST CASE UserDAO ==========");

        // Dùng timestamp để tạo chuỗi ngẫu nhiên, tránh lỗi Duplicate Entry khi chạy nhiều lần
        long time = System.currentTimeMillis(); 
        String testUser = "test_user_" + time;
        String testEmail = "test" + time + "@gmail.com";
        String testPass = "123456";

        // [Test 1] Đăng ký tài khoản (Dùng String)
        System.out.println("\n[1] Đang test hàm Đăng ký (String)...");
        boolean isRegistered = dao.registerUser(testUser, testPass, testEmail, "BIDDER");
        System.out.println("-> Kết quả: " + (isRegistered ? "PASSED ✅" : "FAILED ❌"));

        // [Test 2] Đăng nhập với thông tin vừa tạo (Pass chuẩn)
        System.out.println("\n[2] Đang test hàm Đăng nhập (Mật khẩu đúng)...");
        String roleSuccess = dao.loginUser(testEmail, testPass);
        System.out.println("-> Kết quả: " + ("BIDDER".equals(roleSuccess) ? "PASSED ✅ (Role: " + roleSuccess + ")" : "FAILED ❌"));

        // [Test 3] Đăng nhập với mật khẩu sai
        System.out.println("\n[3] Đang test hàm Đăng nhập (Mật khẩu sai)...");
        String roleFail = dao.loginUser(testEmail, "mat_khau_bay_ba");
        System.out.println("-> Kết quả: " + (roleFail == null ? "PASSED ✅ (Đã chặn được)" : "FAILED ❌"));

        // [Test 4] Đổi mật khẩu
        System.out.println("\n[4] Đang test hàm Đổi mật khẩu...");
        String newPass = "mat_khau_sieu_cap_vip_pro";
        boolean isUpdated = dao.updatePassword(testEmail, newPass);
        System.out.println("-> Kết quả đổi MK: " + (isUpdated ? "PASSED ✅" : "FAILED ❌"));

        // [Test 5] Đăng nhập lại bằng Mật khẩu MỚI
        System.out.println("\n[5] Đang test Đăng nhập bằng Mật khẩu MỚI...");
        String roleAfterUpdate = dao.loginUser(testEmail, newPass);
        System.out.println("-> Kết quả: " + ("BIDDER".equals(roleAfterUpdate) ? "PASSED ✅" : "FAILED ❌"));

        // [Test 6] Đăng ký bằng OBJECT 
        System.out.println("\n[6] Đang test hàm Đăng ký bằng Đối tượng (OOP)...");
        
        long oop_time = System.currentTimeMillis();
        Bidder oopBidder = new Bidder(0, "oop_user_" + oop_time, "oop_pass_123", "oop_test_" + oop_time + "@gmail.com", 1000.0);
        
        System.out.println("-> ID trước khi lưu DB: " + oopBidder.getId()); // Sẽ in ra 0
        boolean isOopRegistered = dao.registerUser(oopBidder, "BIDDER");
        System.out.println("-> Kết quả lưu Object: " + (isOopRegistered ? "PASSED ✅" : "FAILED ❌"));
        System.out.println("-> ID sau khi lưu DB (Đã được MySQL cấp): " + oopBidder.getId()); // Sẽ in ra số xịn
        
        System.out.println("\n================ KẾT THÚC TEST ================");
    }
}