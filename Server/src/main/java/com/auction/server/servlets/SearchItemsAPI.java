package com.auction.server.servlets;

import com.auction.controller.ItemController;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SearchItemsAPI extends HttpServlet {

    private final ItemController itemController = new ItemController();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String keyword = req.getParameter("q");
        int page = 1;
        int limit = 10;
        // Lấy page, limit...

        String jsonResponse = itemController.handleSearchItems(keyword, page, limit);
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(jsonResponse);
    }
}