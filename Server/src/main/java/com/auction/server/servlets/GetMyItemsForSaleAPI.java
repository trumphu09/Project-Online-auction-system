package com.auction.server.servlets;

import com.auction.controller.ItemController;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class GetMyItemsForSaleAPI extends HttpServlet {

    private final ItemController itemController = new ItemController();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        
        HttpSession session = req.getSession(false);
        int sellerId = (Integer) session.getAttribute("userId");

        String jsonResponse = itemController.handleGetItemsBySeller(sellerId);
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(jsonResponse);
    }
}