package com.auction.server.servlets;

import com.auction.controller.AuctionController;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.stream.Collectors;

public class PlaceBidAPI extends HttpServlet {

    private final AuctionController auctionController = new AuctionController();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        // 1. Lấy userId an toàn từ session (đã được AuthFilter đảm bảo)
        HttpSession session = req.getSession(false);
        int userId = (Integer) session.getAttribute("userId");

        // 2. Đọc toàn bộ body của request thành một chuỗi
        String jsonRequest = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

        // 3. Chuyển tiếp cho Controller xử lý
        String jsonResponse = auctionController.handlePlaceBid(jsonRequest, userId);

        // 4. Ghi kết quả trả về từ Controller ra response
        resp.getWriter().write(jsonResponse);
    }
}