package com.auction.server.dao;

import com.auction.server.models.User;
import com.auction.server.models.Bidder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BidderSubDAO implements IUserSubDAO {
    
    @Override
    public boolean insertSubUser(Connection conn, User user, int userId) throws SQLException {
        String sql = "INSERT INTO bidders (user_id, account_balance) VALUES (?, ?)";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, userId);
            pst.setDouble(2, 0.0); // Mặc định số dư tài khoản mới là 0
            return pst.executeUpdate() > 0;
        }
    }

    @Override
    public User getSubUser(Connection conn, int userId, String username, String password, String email) throws SQLException {
        String sql = "SELECT account_balance FROM bidders WHERE user_id = ?";
        try (PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setInt(1, userId);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    // Khởi tạo đối tượng Bidder (Dùng constructor thứ 2 có ID trong User)
                    return new Bidder(userId, username, password, email); 
                }
            }
        }
        return null;
    }
}