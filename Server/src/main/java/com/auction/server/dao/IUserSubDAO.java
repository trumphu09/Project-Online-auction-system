package com.auction.server.dao;

import com.auction.server.models.User;
import java.sql.Connection;
import java.sql.SQLException;

public interface IUserSubDAO {
    // Xử lý chèn dữ liệu vào bảng con (bidders, sellers...)
    boolean insertSubUser(Connection conn, User user, int userId) throws SQLException;
    
    // Xử lý lấy và map dữ liệu từ bảng con thành đối tượng User cụ thể (Bidder, Seller)
    User getSubUser(Connection conn, int userId, String username, String password, String email) throws SQLException;
}