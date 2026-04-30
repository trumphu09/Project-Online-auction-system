package com.auction.server.servlets;

import com.auction.controller.ItemController;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AdminItemAPI extends HttpServlet {

    private final ItemController itemController = new ItemController();

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();
        String[] pathParts = (pathInfo != null) ? pathInfo.split("/") : new String[0];

        if (pathParts.length != 2) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"URL không hợp lệ. Định dạng đúng: /api/admin/items/{itemId}\"}");
            return;
        }

        try {
            int itemId = Integer.parseInt(pathParts[1]);
            String jsonResponse = itemController.handleDeleteItem(itemId);

            if (jsonResponse.contains("\"status\":\"success\"")) {
                resp.setStatus(HttpServletResponse.SC_OK);
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
            resp.getWriter().write(jsonResponse);

        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"ID sản phẩm không hợp lệ.\"}");
        }
    }
}