package com.auction.server.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // 1. Biến instance duy nhất tĩnh (Singleton)
    private static DatabaseConnection instance;
    private Connection connection;

    // 2. Thông tin cấu hình Database
    private final String URL = "jdbc:mysql://localhost:3306/auction_db";
    private final String USER = "root"; 
    // CHÚ Ý: Đổi chữ "mat_khau_cua_ban" thành mật khẩu root bạn vừa cài lúc nãy!
    private final String PASSWORD = "123456"; 

    // 3. Constructor được để ở chế độ private để ngăn tạo đối tượng lung tung
    private DatabaseConnection() {
        try {
            // Đăng ký Driver của MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Mở kết nối
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("-> [Thành công] Server đã kết nối với MySQL (auction_db)!");
        } catch (ClassNotFoundException e) {
            System.err.println("-> [Lỗi] Không tìm thấy thư viện MySQL JDBC. Kiểm tra lại file pom.xml");
        } catch (SQLException e) {
            System.err.println("-> [Lỗi] Không kết nối được Database. Sai mật khẩu hoặc MySQL chưa bật?");
            e.printStackTrace();
        }
    }

    // 4. Hàm cung cấp điểm truy cập duy nhất
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    // 5. Hàm lấy đường ống kết nối ra để dùng
    public Connection getConnection() {
        return connection;
    }

    // --- HÀM MAIN ĐỂ BẠN CHẠY THỬ KIỂM TRA NGAY TẠI CHỖ ---
    public static void main(String[] args) {
        // Gọi thử để xem console có in ra chữ thành công không
        DatabaseConnection db = DatabaseConnection.getInstance();
        Connection conn = db.getConnection();
        
        if (conn != null) {
            System.out.println("Kiểm tra: Đường ống hoạt động tốt, sẵn sàng truyền dữ liệu!");
        }
    }
}
