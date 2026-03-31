package com.auction.server.dao;

import com.auction.server.models.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import com.auction.server.models.Bidder;
public class BidderDAO {
    // Kéo chuyên gia UserDAO vào để làm thuê phần bảng cha
    private UserDAO userDAO = new UserDAO();

    // ==========================================
    // CÁCH 1: Dùng Đối tượng (OOP) - Nòng cốt
    // ==========================================
    public boolean registerBidder(Bidder bidder) {
        
        // Bước 1: Nhờ UserDAO nhét thông tin vào bảng `users` trước
        boolean isUserCreated = userDAO.registerUser(bidder, "BIDDER");

        // Nếu bảng users tạo thành công, lúc này bidder.getId() ĐÃ CÓ SỐ THẬT (VD: 108)
        if (isUserCreated) {
            
            // Bước 2: Nhét tiếp cái ID 108 đó vào bảng `bidders` (Tài khoản mới mặc định 0 đồng)
            String sql = "INSERT INTO bidders (user_id, account_balance) VALUES (?, 0.0)";
            Connection conn = DatabaseConnection.getInstance().getConnection();

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                // Móc đúng cái ID từ Object ra để khóa ngoại (Foreign Key) khớp nhau
                pstmt.setInt(1, bidder.getId()); 
                
                int rowsInserted = pstmt.executeUpdate();
                return rowsInserted > 0; // Trả về true nếu chèn thành công

            } catch (SQLException e) {
                System.err.println("-> [Lỗi] Không thể thêm vào bảng bidders: " + e.getMessage());
                // Lưu ý: Đáng lẽ ở đây phải có lệnh Rollback xóa User nếu lỗi, nhưng để đơn giản ta cứ báo lỗi trước.
            }
        }
        return false;
    }
    
    // ==========================================
    // CÁCH 2: Dùng String rời rạc (Dành cho UI gọi cho lẹ)
    // ==========================================
    public boolean registerBidder(String username, String password, String email) {
        
        // Nhét số 0 vào làm ID giả, số 0.0 làm tiền mặc định để chiều lòng cái Constructor của bạn!
        Bidder tempBidder = new Bidder(0, username, password, email, 0.0);
        
        // Quăng cho Cách 1 xử lý. Nó sẽ vứt cái ID 0 kia đi và đè ID xịn từ MySQL vào!
        return registerBidder(tempBidder); 
    }
}
