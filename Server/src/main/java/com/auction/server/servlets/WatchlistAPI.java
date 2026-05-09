package com.auction.server.servlets;

import com.auction.controller.WatchlistController;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

public class WatchlistAPI extends HttpServlet {
  private final Gson gson = new Gson();
  private final WatchlistController watchlistController = WatchlistController.getInstance();

  // Xử lý API Lấy danh sách giỏ hàng (GET request)
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    resp.setContentType("application/json");
    resp.setCharacterEncoding("UTF-8");

    try {
      // Lấy userId từ AuthFilter (Filter của nhóm Đại thường ép sẵn userId vào Attribute khi đã đăng nhập)
      int userId = (int) req.getAttribute("userId");

      Map<String, Object> result = watchlistController.handleGetWatchlist(userId);
      resp.getWriter().write(gson.toJson(result));

    } catch (Exception e) {
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      resp.getWriter().write("{\"status\":\"error\", \"message\":\"Lỗi lấy giỏ hàng\"}");
    }
  }

  // Xử lý API Thêm vào giỏ hàng (POST request)
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    resp.setContentType("application/json");
    resp.setCharacterEncoding("UTF-8");

    try {
      int userId = (int) req.getAttribute("userId"); // Lấy người đang đăng nhập

      // Đọc dữ liệu Front-end gửi lên (chứa itemId)
      JsonObject requestBody = gson.fromJson(req.getReader(), JsonObject.class);
      int itemId = requestBody.get("itemId").getAsInt();

      Map<String, Object> result = watchlistController.handleAddToWatchlist(userId, itemId);
      resp.getWriter().write(gson.toJson(result));

    } catch (Exception e) {
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      resp.getWriter().write("{\"status\":\"error\", \"message\":\"Dữ liệu gửi lên không hợp lệ\"}");
    }
  }
}