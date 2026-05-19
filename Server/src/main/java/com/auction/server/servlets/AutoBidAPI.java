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
        Map<String, Object> data = autoBidDAO.getAutoBidStatus(auctionId, userId);

        JsonObject root = new JsonObject();
        root.addProperty("status", "success");
        root.add("data", gson.toJsonTree(data));

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
                : 50000.0; // Default

        System.out.println("[AutoBidAPI.doPost] Received: auctionId=" + auctionId + 
            ", userId=" + userId + ", maxAmount=" + maxAmount + ", priceStep=" + priceStep);

        if (maxAmount <= 0) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"max_amount phải > 0\"}");
            return;
        }

        if (priceStep <= 0) priceStep = 50000.0;

        boolean ok = autoBidDAO.upsertAutoBid(auctionId, userId, maxAmount, priceStep);
        resp.getWriter().write(ok
                ? "{\"status\":\"success\",\"message\":\"Đã bật auto-bid\"}"
                : "{\"status\":\"error\",\"message\":\"Không lưu được auto-bid\"}");
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
        boolean ok = autoBidDAO.removeAutoBid(auctionId, userId);
        resp.getWriter().write(ok
                ? "{\"status\":\"success\",\"message\":\"Đã tắt auto-bid\"}"
                : "{\"status\":\"error\",\"message\":\"Không tắt được auto-bid\"}");
    }
}