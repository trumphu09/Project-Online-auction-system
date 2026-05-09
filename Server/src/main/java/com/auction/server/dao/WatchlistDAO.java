package com.auction.server.dao;

import com.auction.server.models.ItemDTO;
import com.auction.server.dao.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WatchlistDAO {
  private static WatchlistDAO instance;

  private WatchlistDAO() {}

  public static WatchlistDAO getInstance() {
    if (instance == null) {
      instance = new WatchlistDAO();
    }
    return instance;
  }

  // 1. Thêm sản phẩm vào giỏ hàng
  public boolean addToWatchlist(int userId, int itemId) {
    String sql = "INSERT INTO watchlist (user_id, item_id) VALUES (?, ?)";
    try (Connection conn = DatabaseConnection.getInstance().getConnection(); // Chú ý: Import đúng DatabaseConnection của nhóm
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, userId);
      stmt.setInt(2, itemId);
      return stmt.executeUpdate() > 0;

    } catch (SQLException e) {
      System.err.println("Lỗi thêm watchlist (Có thể do đã tồn tại trong giỏ): " + e.getMessage());
      return false;
    }
  }

  // 2. Lấy danh sách sản phẩm trong giỏ hàng của 1 User
  public List<ItemDTO> getWatchlistByUserId(int userId) {
    List<ItemDTO> list = new ArrayList<>();
    // Lấy toàn bộ thông tin tĩnh của Items dựa vào Giỏ hàng của User
    String sql = "SELECT i.* FROM items i JOIN watchlist w ON i.id = w.item_id WHERE w.user_id = ?";

    try (Connection conn = DatabaseConnection.getInstance().getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, userId);
      ResultSet rs = stmt.executeQuery();

      while (rs.next()) {
        ItemDTO item = new ItemDTO();

        // MAPPING CHUẨN XÁC THEO ItemDTO BẠN CUNG CẤP
        item.setId(rs.getInt("id"));
        item.setSellerId(rs.getInt("seller_id"));
        item.setName(rs.getString("name"));
        item.setDescription(rs.getString("description"));
        item.setStartingPrice(rs.getDouble("starting_price"));
        item.setCategory(rs.getString("category"));
        item.setImagePath(rs.getString("image_path"));

        // Cột created_at trong DB thường là TIMESTAMP, gọi getString() để khớp với String createdAt trong DTO
        if (rs.getTimestamp("created_at") != null) {
          item.setCreatedAt(rs.getTimestamp("created_at").toString());
        }

        list.add(item);
      }
    } catch (SQLException e) {
      System.err.println("Lỗi lấy danh sách watchlist: " + e.getMessage());
    }
    return list;
  }

  // 3. (Tùy chọn) Xóa một sản phẩm khỏi giỏ hàng
  public boolean removeFromWatchlist(int userId, int itemId) {
    String sql = "DELETE FROM watchlist WHERE user_id = ? AND item_id = ?";
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, userId);
      stmt.setInt(2, itemId);
      return stmt.executeUpdate() > 0;

    } catch (SQLException e) {
      System.err.println("Lỗi xóa khỏi watchlist: " + e.getMessage());
      return false;
    }
  }
}