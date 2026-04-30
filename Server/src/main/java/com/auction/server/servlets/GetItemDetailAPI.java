package com.auction.server.servlets;

import com.auction.controller.ItemController;
import com.auction.server.dao.BidsDAO;
import com.auction.server.models.BidTransactionDTO;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class GetItemDetailAPI extends HttpServlet {

    private final ItemController itemController = new ItemController();
    private final BidsDAO bidsDAO = new BidsDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Thiếu ID của sản phẩm.\"}");
            return;
        }

        String[] pathParts = pathInfo.split("/");

        try {
            if (pathParts.length == 3 && "bids".equals(pathParts[2])) {
                int itemId = Integer.parseInt(pathParts[1]);
                List<BidTransactionDTO> history = bidsDAO.getBidHistoryByItemId(itemId);
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write(gson.toJson(history));
                return;
            }

            if (pathParts.length == 2) {
                int itemId = Integer.parseInt(pathParts[1]);
                String jsonResponse = itemController.handleGetItemDetails(itemId);
                
                JsonObject respObj = gson.fromJson(jsonResponse, JsonObject.class);
                if (respObj != null && "success".equals(respObj.get("status").getAsString())) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                }
                resp.getWriter().write(jsonResponse);
                return;
            }

            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("{\"error\":\"URL không hợp lệ.\"}");

        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"ID sản phẩm không hợp lệ.\"}");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        int sellerId = (Integer) session.getAttribute("userId");

        String pathInfo = req.getPathInfo();
        String[] pathParts = (pathInfo != null) ? pathInfo.split("/") : new String[0];

        if (pathParts.length != 2) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"URL không hợp lệ. Định dạng đúng: /api/items/{itemId}\"}");
            return;
        }

        try {
            int itemId = Integer.parseInt(pathParts[1]);
            String requestBody = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            
            String jsonResponse = itemController.handleUpdateItem(requestBody, itemId, sellerId);

            JsonObject respObj = gson.fromJson(jsonResponse, JsonObject.class);
            if (respObj != null && "success".equals(respObj.get("status").getAsString())) {
                resp.setStatus(HttpServletResponse.SC_OK);
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
            resp.getWriter().write(jsonResponse);

        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"ID sản phẩm không hợp lệ.\"}");
        }
    }
}