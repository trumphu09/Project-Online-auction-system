package com.auction.server.servlets;

import com.auction.server.dao.AutoBidDAO;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.servlet.http.*;
import java.io.IOException;
import java.util.Map;

public class AutoBidAPI extends HttpServlet {
    private final Gson gson = new Gson();
    private final AutoBidDAO autoBidDAO = AutoBidDAO.getInstance();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Chưa đăng nhập\"}");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");
        String auctionIdStr = req.getParameter("auction_id");
        if (auctionIdStr == null || auctionIdStr.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Thiếu auction_id\"}");
            return;
        }

        int auctionId = Integer.parseInt(auctionIdStr);
        System.out.println("[AutoBidAPI.doGet] userId=" + userId + ", auctionId=" + auctionId);
        
        Map<String, Object> data = autoBidDAO.getAutoBidStatus(auctionId, userId);

        JsonObject root = new JsonObject();
        root.addProperty("status", "success");
        root.add("data", gson.toJsonTree(data));
        
        System.out.println("[AutoBidAPI.doGet] Result: active=" + data.get("active") + 
                ", maxAmount=" + data.get("max_amount"));

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(gson.toJson(root));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Chưa đăng nhập\"}");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");
        JsonObject body = gson.fromJson(req.getReader(), JsonObject.class);

        int auctionId = body.get("auction_id").getAsInt();
        double maxAmount = body.get("max_amount").getAsDouble();
        double priceStep = body.has("price_step") && !body.get("price_step").isJsonNull()
                ? body.get("price_step").getAsDouble()
                : 50000.0;

        System.out.println("[AutoBidAPI.doPost] userId=" + userId + ", auctionId=" + auctionId + 
                ", maxAmount=" + maxAmount + ", priceStep=" + priceStep);

        if (maxAmount <= 0) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"max_amount phải > 0\"}");
            return;
        }

        if (priceStep <= 0) priceStep = 50000.0;

        boolean ok = autoBidDAO.upsertAutoBid(auctionId, userId, maxAmount, priceStep);
        if (!ok) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            System.out.println("[AutoBidAPI.doPost] FAILED to save auto-bid");
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Không lưu được auto-bid\"}");
            return;
        }

        System.out.println("[AutoBidAPI.doPost] Auto-bid saved, triggering auto-bid resolver...");

        // Kích hoạt auto-bid ngay sau khi bật
        boolean autoBidExecuted = com.auction.service.AuctionService
                .getInstance()
                .resolveAutoBidsAfterCurrentState(auctionId);

        System.out.println("[AutoBidAPI.doPost] Auto-bid executed: " + autoBidExecuted);

        JsonObject data = new JsonObject();
        data.addProperty("active", true);
        data.addProperty("auto_bid_executed", autoBidExecuted);

        JsonObject root = new JsonObject();
        resp.setStatus(HttpServletResponse.SC_OK);
        root.addProperty("status", "success");
        root.addProperty("message", autoBidExecuted
                ? "Đã bật auto-bid và hệ thống đã tự đẩy giá nếu đủ điều kiện."
                : "Đã bật auto-bid, nhưng hiện chưa đủ điều kiện để tự đặt giá mới.");
        root.add("data", data);

        resp.getWriter().write(gson.toJson(root));
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Chưa đăng nhập\"}");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");
        String auctionIdStr = req.getParameter("auction_id");
        if (auctionIdStr == null || auctionIdStr.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Thiếu auction_id\"}");
            return;
        }

        int auctionId = Integer.parseInt(auctionIdStr);
        System.out.println("[AutoBidAPI.doDelete] userId=" + userId + ", auctionId=" + auctionId);
        
        boolean ok = autoBidDAO.removeAutoBid(auctionId, userId);
        
        if (ok) {
            resp.setStatus(HttpServletResponse.SC_OK);
            System.out.println("[AutoBidAPI.doDelete] SUCCESS - Auto-bid removed for userId=" + userId + ", auctionId=" + auctionId);
            resp.getWriter().write("{\"status\":\"success\",\"message\":\"Đã tắt auto-bid\"}");
        } else {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            System.out.println("[AutoBidAPI.doDelete] FAILED - Could not remove auto-bid for userId=" + userId + ", auctionId=" + auctionId);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Không tắt được auto-bid\"}");
        }
    }
}