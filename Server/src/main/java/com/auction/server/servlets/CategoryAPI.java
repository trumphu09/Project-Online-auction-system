package com.auction.server.servlets;

import com.auction.controller.ItemController;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class CategoryAPI extends HttpServlet {

    private final ItemController itemController = new ItemController();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();
        String jsonResponse;

        if (pathInfo == null || pathInfo.equals("/")) {
            jsonResponse = itemController.handleGetCategories();
        } else {
            String[] pathParts = pathInfo.split("/");
            if (pathParts.length == 3 && "items".equals(pathParts[2])) {
                String categoryName = URLDecoder.decode(pathParts[1], StandardCharsets.UTF_8.name());
                int page = 1;
                int limit = 10;
                // Lấy page, limit từ request params...
                jsonResponse = itemController.handleGetItemsByCategory(categoryName, page, limit);
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"URL không hợp lệ.\"}");
                return;
            }
        }
        
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(jsonResponse);
    }
}