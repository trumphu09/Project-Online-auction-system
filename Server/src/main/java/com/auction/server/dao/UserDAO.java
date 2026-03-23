package com.auction.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserDAO {

    public boolean registerUser(String username, String password, String role) {
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";

        // 1. Lấy đường ống kết nối ra NGOÀI khối try (Tuyệt đối không đóng nó lại)
        Connection conn = DatabaseConnection.getInstance().getConnection();

        // 2. Chỉ cho PreparedStatement vào trong ngoặc để nó tự đóng dọn rác sau khi chạy lệnh
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, role); 

            int rowsInserted = pstmt.executeUpdate();
            return rowsInserted > 0; 

        } catch (SQLException e) {
            System.err.println("-> [Loi] Khong the them nguoi dung (" + username + "): " + e.getMessage());
            return false;
        }
    }

    // --- HÀM MAIN ĐỂ CHẠY TEST ---
    public static void main(String[] args) {
        UserDAO dao = new UserDAO();
        
        // Lưu ý: Đổi tên một chút vì tài khoản "thanh_bidder" lúc nãy CÓ THỂ ĐÃ KỊP LƯU VÀO TRƯỚC KHI BỊ ĐÓNG CỬA rồi!
        // Nếu tên bị trùng, MySQL sẽ báo lỗi "Duplicate entry" vì cột username cấu hình là UNIQUE.
        boolean test1 = dao.registerUser("thanh_bidder_2", "123456", "BIDDER");
        boolean test2 = dao.registerUser("phu_seller_2", "123456", "SELLER");
        boolean test3 = dao.registerUser("admin_vip_2", "admin123", "ADMIN");

        if (test1 && test2 && test3) {
            System.out.println("-> Tuyet voi! Da bom thanh cong 3 tai khoan vao Database!");
        } else {
            System.out.println("-> Xong! Co tai khoan da ton tai hoac co loi xay ra.");
        }
    }
}