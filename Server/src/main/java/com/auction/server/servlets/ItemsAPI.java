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

    // BỔ SUNG HÀM GET ĐỂ CLIENT LẤY ĐƯỢC DANH SÁCH SẢN PHẨM Ở TRANG CHỦ
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        int page = 1;
        int limit = 10;
        try {
            if (req.getParameter("page") != null) page = Integer.parseInt(req.getParameter("page"));
            if (req.getParameter("limit") != null) limit = Integer.parseInt(req.getParameter("limit"));
        } catch (NumberFormatException e) { }
        
        String jsonResponse = itemController.handleListItems(page, limit);
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(jsonResponse);
    }

    // HÀM POST GIỮ NGUYÊN NHƯ CŨ 
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        HttpSession session = req.getSession(false);
        int sellerId = (Integer) session.getAttribute("userId");
        String requestBody = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        
        String jsonResponse = itemController.handleAddItem(requestBody, sellerId);
        if (jsonResponse.contains("\"status\":\"success\"")) {
            resp.setStatus(HttpServletResponse.SC_CREATED);
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
        resp.getWriter().write(jsonResponse);
    }
}