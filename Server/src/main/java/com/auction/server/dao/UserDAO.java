package com.auction.server.dao;

import com.auction.server.models.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
            pstmt.setString(2,password);

            try (java.sql.ResultSet rs = pstmt.executeQuery()){
                if (rs.next()){
                    return rs.getString("role");
                }
            }

        }catch(SQLException e){
        System.err.println("user doesn't exist" + e.getMessage());
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
                        
                        return true; // Báo cáo thành công!
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("-> [Lỗi] Không thể đăng ký User (" + user.getUsername() + "): " + e.getMessage());
        }
        return false;
    }

    // --- HÀM MAIN ĐỂ CHẠY TEST ---
    public static void main(String[] args) {
        UserDAO dao = new UserDAO();
        
        boolean test1 = dao.registerUser("thanh_bidder_1", "123456","pt0907@gmail.com", "BIDDER");
        boolean test2 = dao.registerUser("dai_seller_1", "123456","dai123@gmail.com", "SELLER");
        boolean test3 = dao.registerUser("toan_admin_1", "admin123","tingre123@gmail.com", "ADMIN");

        if (test1 && test2 && test3) {
            System.out.println("-> Tuyet voi! Da bom thanh cong 3 tai khoan vao Database!");
        } else {
            System.out.println("-> Xong! Co tai khoan da ton tai hoac co loi xay ra.");
        }
    }
}