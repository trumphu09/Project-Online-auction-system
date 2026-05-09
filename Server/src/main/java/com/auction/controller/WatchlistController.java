package com.auction.controller;

import com.auction.server.dao.WatchlistDAO;
import com.auction.server.models.ItemDTO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WatchlistController {
  private static WatchlistController instance;

  private WatchlistController() {}

  public static WatchlistController getInstance() {
    if (instance == null) {
      instance = new WatchlistController();
    }
    return instance;
  }

  // 1. Hàm xử lý khi Client muốn Thêm vào giỏ
  public Map<String, Object> handleAddToWatchlist(int userId, int itemId) {
    Map<String, Object> response = new HashMap<>();

    boolean success = WatchlistDAO.getInstance().addToWatchlist(userId, itemId);

    if (success) {
      response.put("status", "success");
      response.put("message", "Đã thêm sản phẩm vào danh sách theo dõi!");
    } else {
      response.put("status", "error");
      response.put("message", "Sản phẩm đã có trong giỏ hoặc ID không hợp lệ.");
    }
    return response;
  }

  // 2. Hàm xử lý khi Client mở thanh Giỏ hàng (Cần lấy danh sách)
  public Map<String, Object> handleGetWatchlist(int userId) {
    Map<String, Object> response = new HashMap<>();

    List<ItemDTO> items = WatchlistDAO.getInstance().getWatchlistByUserId(userId);

    response.put("status", "success");
    response.put("data", items); // Ném nguyên cái List vào đây để gửi về Client
    return response;
  }
}