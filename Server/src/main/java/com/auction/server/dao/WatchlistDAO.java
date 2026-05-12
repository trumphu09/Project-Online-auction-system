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
    try (Connection conn = DatabaseConnection.getInstance().getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      System.out.println("[WatchlistDAO] Adding to watchlist - userId: " + userId + ", itemId: " + itemId);
      
      stmt.setInt(1, userId);
      stmt.setInt(2, itemId);
      int result = stmt.executeUpdate();
      
      System.out.println("[WatchlistDAO] Insert result: " + result);
      return result > 0;

    } catch (SQLException e) {
      System.err.println("[WatchlistDAO ERROR] Lỗi thêm watchlist - Error code: " + e.getErrorCode() + ", Message: " + e.getMessage());
      e.printStackTrace();
      // Kiểm tra xem có phải là lỗi trùng khóa (Duplicate entry) không
      if (e.getMessage().contains("Duplicate entry")) {
        System.err.println("[WatchlistDAO] Sản phẩm đã tồn tại trong giỏ");
      }
      return false;
    } catch (Exception e) {
      System.err.println("[WatchlistDAO ERROR] Exception: " + e.getClass().getName() + " - " + e.getMessage());
      e.printStackTrace();
      return false;
    }
  }

  // 2. Lấy danh sách sản phẩm trong giỏ hàng của 1 User
  public List<ItemDTO> getWatchlistByUserId(int userId) {
    List<ItemDTO> list = new ArrayList<>();
    // JOIN với auctions để lấy price_step và các thông tin đấu giá
    String sql = "SELECT i.*, a.id as auction_id, a.price_step, a.current_max_price, a.highest_bidder_id, a.status, a.start_time, a.end_time " +
                 "FROM items i " +
                 "JOIN watchlist w ON i.id = w.item_id " +
                 "LEFT JOIN auctions a ON i.id = a.item_id " +
                 "WHERE w.user_id = ?";

    try (Connection conn = DatabaseConnection.getInstance().getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      System.out.println("[WatchlistDAO] Getting watchlist for userId: " + userId);
      
      stmt.setInt(1, userId);
      ResultSet rs = stmt.executeQuery();

      int count = 0;
      while (rs.next()) {
        ItemDTO item = new ItemDTO();

        // MAPPING CHUẨN XÁC THEO ItemDTO
        item.setId(rs.getInt("id"));
        item.setSellerId(rs.getInt("seller_id"));
        item.setName(rs.getString("name"));
        item.setDescription(rs.getString("description"));
        item.setStartingPrice(rs.getDouble("starting_price"));
        item.setCategory(rs.getString("category"));
        item.setImagePath(rs.getString("image_path"));

        // Cột created_at trong DB thường là TIMESTAMP
        if (rs.getTimestamp("created_at") != null) {
          item.setCreatedAt(rs.getTimestamp("created_at").toString());
        }
        
        // Lấy thông tin từ auctions (có thể null nếu chưa có đấu giá)
        int auctionId = rs.getInt("auction_id");
        if (auctionId > 0) {
          item.setAuctionId(auctionId);
          item.setPriceStep(rs.getDouble("price_step"));
          item.setCurrentMaxPrice(rs.getDouble("current_max_price"));
          item.setHighestBidderId(rs.getInt("highest_bidder_id"));
          item.setStatus(rs.getString("status"));
          
          if (rs.getTimestamp("start_time") != null) {
            item.setStartTime(rs.getTimestamp("start_time").toString());
          }
          if (rs.getTimestamp("end_time") != null) {
            item.setEndTime(rs.getTimestamp("end_time").toString());
          }
        }

        list.add(item);
        count++;
      }
      
      System.out.println("[WatchlistDAO] Retrieved " + count + " items from watchlist for userId: " + userId);
      
    } catch (SQLException e) {
      System.err.println("[WatchlistDAO ERROR] Lỗi lấy danh sách watchlist - " + e.getMessage());
      e.printStackTrace();
    } catch (Exception e) {
      System.err.println("[WatchlistDAO ERROR] Exception: " + e.getClass().getName() + " - " + e.getMessage());
      e.printStackTrace();
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