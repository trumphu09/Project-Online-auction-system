package com.auction.server.dao;

import com.auction.server.models.User;
import com.auction.server.models.Seller;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SellerSubDAO implements IUserSubDAO {
    
    @Override
    public boolean insertSubUser(Connection conn, User user, int userId) throws SQLException {
        String sql = "INSERT INTO sellers (user_id, total_rating, sale_count, account_balance) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, userId);
            pst.setDouble(2, 0.0); // total_rating
            pst.setInt(3, 0);      // sale_count
            pst.setDouble(4, 0.0); // account_balance
            return pst.executeUpdate() > 0;
        }
    }

    @Override
    public User getSubUser(Connection conn, int userId, String username, String password, String email) throws SQLException {
        String sql = "SELECT total_rating, sale_count, account_balance FROM sellers WHERE user_id = ?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, userId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    // Khởi tạo đối tượng Seller (Dùng constructor thứ 2 có ID trong User)
                    return new Seller(userId, username, password, email);
                }
            }
        }
        return null;
    }
}