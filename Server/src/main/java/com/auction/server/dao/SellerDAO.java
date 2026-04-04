package com.auction.server.dao;

import com.auction.server.models.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.auction.server.models.Bidder;
import com.auction.server.models.Seller;
public class SellerDAO {
    // Kéo chuyên gia UserDAO vào để làm thuê phần bảng cha
    private UserDAO userDAO = new UserDAO();
    public boolean registerSeller(Seller seller) {
        
        boolean isUserCreated = userDAO.registerUser(seller, "SELLER");

        if (isUserCreated) {
            
            String sql = "INSERT INTO bidders (user_id, account_balance) VALUES (?, 0.0)";
            Connection conn = DatabaseConnection.getInstance().getConnection();

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setInt(1, seller.getId()); 
                
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
        Seller tempSeller = new Seller(0, username, password, email);
        
        // Quăng cho Cách 1 xử lý. Nó sẽ vứt cái ID 0 kia đi và đè ID xịn từ MySQL vào!
        return registerSeller(tempSeller); 
    }

    // lay seller tu id
    public Seller getBidderById(int sellerId){
        String sql = "SELLECT u.id, u.username, u.password, u.email, s.accountbalance, s.total_rating, s.sale_count"+
                     "FROM users u JOIN sellers s ON u.id = s.user_id WHERE u.id = ?";   

        Connection conn = DatabaseConnection.getInstance().getConnection();
        try(PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1,sellerId);
            try(java.sql.ResultSet rs = pstmt.executeQuery()){
                if(rs.next()){
                    int id = rs.getInt("id");
                    String username = rs.getString("username");
                    String password = rs.getString("password");
                    String email = rs.getString("email");
                    double accountbalance = rs.getDouble("account_balance");
                    int sale_count = rs.getInt("sale_count");
                    double total_rating = rs.getDouble("total_rating");
                    return new Seller(id, username, password, email,total_rating, accountbalance, sale_count);
                }
            }
        }catch(SQLException e){
            return null;
        }
        return null;
    }
    
    // update Rating after an user buy an item
    public boolean rateSeller(int sellerId, double newScore) {
        
        if (newScore < 0.0) newScore = 0.0;
        if (newScore > 5.0) newScore = 5.0;
        
        String sql = "UPDATE sellers " +
                     "SET seller_rating = ((seller_rating * sale_count) + ?) / (sale_count + 1), " +
                     "sale_count = sale_count + 1 " +
                     "WHERE user_id = ?"; 
        Connection conn = DatabaseConnection.getInstance().getConnection();
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDouble(1, newScore); // Truyền điểm số đánh giá mới vào công thức
            pstmt.setInt(2, sellerId);    // Truyền ID người bán
            
            int rowsUpdated = pstmt.executeUpdate();
            
            return rowsUpdated > 0; 
            
        } catch (SQLException e) {
            return false;
        }
    }
}
