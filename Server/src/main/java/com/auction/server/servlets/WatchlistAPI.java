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
import java.util.stream.Collectors;

public class WatchlistAPI extends HttpServlet {
  private final Gson gson = new Gson();
  private final WatchlistController watchlistController = WatchlistController.getInstance();

  // Xử lý API Lấy danh sách giỏ hàng (GET request)
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    resp.setContentType("application/json");
    resp.setCharacterEncoding("UTF-8");

    try {
      // Lấy userId từ AuthFilter (Session đã được set khi đăng nhập)
      Object userIdObj = req.getAttribute("userId");
      if (userIdObj == null) {
        // Nếu không có userId từ attribute, kiểm tra từ session
        userIdObj = req.getSession(false) != null ? req.getSession().getAttribute("userId") : null;
      }
      
      if (userIdObj == null) {
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        resp.getWriter().write("{\"status\":\"error\", \"message\":\"Bạn cần đăng nhập\"}");
        return;
      }
      
      int userId = (int) userIdObj;
      Map<String, Object> result = watchlistController.handleGetWatchlist(userId);
      resp.getWriter().write(gson.toJson(result));

    } catch (Exception e) {
      System.err.println("[Watchlist GET ERROR] " + e.getMessage());
      e.printStackTrace();
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      resp.getWriter().write("{\"status\":\"error\", \"message\":\"Lỗi lấy giỏ hàng: " + e.getMessage() + "\"}");
    }
  }

  // Xử lý API Thêm vào giỏ hàng (POST request)
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    resp.setContentType("application/json");
    resp.setCharacterEncoding("UTF-8");

    try {
      // Lấy userId từ Attribute hoặc Session
      Object userIdObj = req.getAttribute("userId");
      if (userIdObj == null) {
        userIdObj = req.getSession(false) != null ? req.getSession().getAttribute("userId") : null;
      }
      
      if (userIdObj == null) {
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        resp.getWriter().write("{\"status\":\"error\", \"message\":\"Bạn cần đăng nhập\"}");
        return;
      }
      
      int userId = (int) userIdObj;

      // Đọc dữ liệu Front-end gửi lên (chứa itemId) - Dùng cách đúng như các Servlet khác
      String jsonRequest = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
      System.out.println("[Watchlist POST] Request body: " + jsonRequest);
      
      if (jsonRequest == null || jsonRequest.isBlank()) {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        resp.getWriter().write("{\"status\":\"error\", \"message\":\"Dữ liệu gửi lên không hợp lệ: body trống\"}");
        return;
      }
      
      JsonObject requestBody = gson.fromJson(jsonRequest, JsonObject.class);
      
      if (!requestBody.has("itemId")) {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        resp.getWriter().write("{\"status\":\"error\", \"message\":\"Dữ liệu gửi lên không hợp lệ: thiếu itemId\"}");
        return;
      }
      
      int itemId = requestBody.get("itemId").getAsInt();
      System.out.println("[Watchlist POST] UserId: " + userId + ", ItemId: " + itemId);

      Map<String, Object> result = watchlistController.handleAddToWatchlist(userId, itemId);
      resp.getWriter().write(gson.toJson(result));

    } catch (com.google.gson.JsonSyntaxException e) {
      System.err.println("[Watchlist POST ERROR - JSON Parse] " + e.getMessage());
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      resp.getWriter().write("{\"status\":\"error\", \"message\":\"Dữ liệu JSON không hợp lệ\"}");
    } catch (Exception e) {
      System.err.println("[Watchlist POST ERROR] " + e.getMessage());
      e.printStackTrace();
      resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      resp.getWriter().write("{\"status\":\"error\", \"message\":\"Dữ liệu gửi lên không hợp lệ: " + e.getMessage() + "\"}");
    }
  }
}