package com.auction.server.servlets;

import com.auction.controller.UserController;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.stream.Collectors;

public class SellerRatingAPI extends HttpServlet {

    private final UserController userController = new UserController();
    private final Gson gson = new Gson();

    // POST /api/my/rating-seller  →  Gửi đánh giá người bán
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Bạn cần đăng nhập.\"}");
            return;
        }

        // Lấy bidderId từ session
        int bidderId = (int) session.getAttribute("userId");

        String jsonRequest = req.getReader().lines()
                .collect(Collectors.joining(System.lineSeparator()));

        // Truyền đủ 2 tham số: jsonRequest + bidderId
        String jsonResponse = userController.handleRateSeller(jsonRequest, bidderId);
        JsonObject obj = gson.fromJson(jsonResponse, JsonObject.class);

        if (obj != null && "success".equalsIgnoreCase(obj.get("status").getAsString())) {
            resp.setStatus(HttpServletResponse.SC_OK);
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        resp.getWriter().write(jsonResponse);
    }

    // GET /api/my/rating-seller?auctionId=X  →  Kiểm tra đã đánh giá chưa
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Bạn cần đăng nhập.\"}");
            return;
        }

        int bidderId = (int) session.getAttribute("userId");

        String auctionIdStr = req.getParameter("auctionId");
        if (auctionIdStr == null || auctionIdStr.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Thiếu auctionId.\"}");
            return;
        }

        try {
            int auctionId = Integer.parseInt(auctionIdStr);
            String jsonResponse = userController.handleHasRatedSeller(auctionId, bidderId);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(jsonResponse);
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"auctionId không hợp lệ.\"}");
        }
    }
}