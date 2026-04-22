package com.auction.server.dao; // Sửa lại đúng package của ông

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnection {

    // Vẫn dùng Singleton, nhưng không phải giữ 1 Connection, mà là giữ 1 cái HỒ CHỨA (DataSource)
    private static DatabaseConnection instance;
    private HikariDataSource dataSource;

    private DatabaseConnection() {
        // Cấu hình Hồ chứa kết nối (Hikari Config)
        HikariConfig config = new HikariConfig();
        
        // Trỏ vào Localhost của ông (Nhớ sửa tên DB hoặc Pass nếu cần)
        config.setJdbcUrl("jdbc:mysql://localhost:3306/online_auction?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        config.setUsername("root");
        config.setPassword("123456"); 

        // ==========================================
        // TINH CHỈNH POOLING (THÔNG SỐ CHUẨN DOANH NGHIỆP)
        // ==========================================
        config.setMaximumPoolSize(50);      // Tối đa 50 kết nối hoạt động cùng lúc
        config.setMinimumIdle(10);          // Lúc rảnh rỗi (ít khách) vẫn giữ 10 kết nối mở sẵn để chờ
        config.setConnectionTimeout(20000); // Nếu có 51 người vào, người thứ 51 phải đợi. Quá 20s không có ống nào rảnh thì báo lỗi (Timeout).
        config.setIdleTimeout(300000);      // Ống nào rảnh quá 5 phút thì đóng bớt cho nhẹ RAM Server
        config.setMaxLifetime(1800000);     // Sau 30 phút, đập ống cũ xây ống mới để tránh lỗi rỉ sét (Memory Leak)

        // Khởi tạo Hồ chứa
        dataSource = new HikariDataSource(config);
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Mỗi lần DAO gọi hàm này, nó KHÔNG MỞ kết nối mới.
     * Nó chỉ MƯỢN 1 kết nối đang có sẵn trong Hồ chứa.
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Khi Server tắt, phải gọi hàm này để xả hồ nước, đóng toàn bộ 50 kết nối lại
     */
    public void closePool() {
        if (dataSource != null) {
            dataSource.close();
            System.out.println("Đã đóng Hồ chứa kết nối Database.");
        }
    }

    // --- HÀM MAIN ĐỂ BẠN CHẠY THỬ KIỂM TRA NGAY TẠI CHỖ ---
    public static void main(String[] args) {
        try {
            // Gọi thử để xem console có in ra chữ thành công không
            DatabaseConnection db = DatabaseConnection.getInstance();
            Connection conn = db.getConnection();
            
            if (conn != null) {
                System.out.println("Kiểm tra: Đường ống HikariCP hoạt động tốt, sẵn sàng truyền dữ liệu!");
                // Nhớ đóng connection trả về hồ sau khi test xong nhé
                conn.close(); 
            }
        } catch (SQLException e) {
            System.err.println("Toang rồi ông giáo ạ, lỗi kết nối: " + e.getMessage());
        }
    }
}
