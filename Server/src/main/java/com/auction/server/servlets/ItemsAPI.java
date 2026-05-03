package com.auction.server.servlets;

import com.auction.controller.ItemController;
import com.google.gson.Gson;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.stream.Collectors;

public class ItemsAPI extends HttpServlet {

    private final ItemController itemController = new ItemController();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        // Filter đã đảm bảo session và role hợp lệ
        int sellerId = (Integer) session.getAttribute("userId");

        String requestBody = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        
        String jsonResponse = itemController.handleAddItem(requestBody, sellerId);
        
        // Dựa vào kết quả từ controller để set status
        if (jsonResponse.contains("\"status\":\"success\"")) {
            resp.setStatus(HttpServletResponse.SC_CREATED);
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
        
        resp.getWriter().write(jsonResponse);
    }

    // doGet có thể được sửa sau để phù hợp với kiến trúc mới nếu cần
    // Hiện tại giữ nguyên để không làm gián đoạn các tính năng khác
}