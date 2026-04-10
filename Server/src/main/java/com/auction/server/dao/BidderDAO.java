package com.auction.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;

import com.auction.server.models.BidTransactionDTO;
import com.auction.server.models.Bidder;
import com.auction.server.models.BidderDTO;
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
                return false;
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

    // lay thong tin cua bidder
    public BidderDTO getBidderById(int bidderId){
    String sql = "SELECT u.id, u.username, u.email, b.account_balance "+
                 "FROM users u JOIN bidders b ON u.id = b.user_id WHERE u.id = ?";   

    Connection conn = DatabaseConnection.getInstance().getConnection();
    try(PreparedStatement pstmt = conn.prepareStatement(sql)){
        pstmt.setInt(1, bidderId);
        try(java.sql.ResultSet rs = pstmt.executeQuery()){
            if(rs.next()){
                int id = rs.getInt("id");
                String username = rs.getString("username");
                String email = rs.getString("email");
                double accountBalance = rs.getDouble("account_balance");
                return new BidderDTO(id, username, email, accountBalance);  // ✅ DTO thay vì Bidder
            }
        }
    }catch(SQLException e){
        return null;
    }
    return null;
    }
    // update account_balance
    // add money to balance
    public boolean updateBalance(int userid, double amount){
        String sql = "UPDATE bidders SET account_balance = account_balance + ? WHERE user_id = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try(PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setDouble(1,amount);
            pstmt.setInt(2,userid);
            
            int rowsUpdated = pstmt.executeUpdate();
            return rowsUpdated > 0; 
        }catch(SQLException e){
            return false;
        }
    }

    //deduct Balance
    public boolean deductBalance(int userId, double amount){
        if(amount < 0){
            amount = Math.abs(amount);
        }
        String sql = "UPDATE bidders SET account_balance = account_balance - ? WHERE user_id = ? AND account_balance > ?";

        Connection conn = DatabaseConnection.getInstance().getConnection();
        try(PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setDouble(1, amount);
            pstmt.setInt(2, userId);
            pstmt.setDouble(3, amount);

            int rowsUpdated = pstmt.executeUpdate();

            return rowsUpdated > 0;
        }catch(SQLException e){
            return false;
        }
    }


}
