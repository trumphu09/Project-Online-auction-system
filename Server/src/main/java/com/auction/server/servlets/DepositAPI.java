package com.auction.server.servlets;

import com.auction.controller.BidderController;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.stream.Collectors;

public class DepositAPI extends HttpServlet {

    private final BidderController bidderController = new BidderController();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        int userId = (Integer) session.getAttribute("userId");

        String jsonRequest = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        
        String jsonResponse = bidderController.handleDeposit(jsonRequest, userId);

        if (jsonResponse.contains("\"status\":\"success\"")) {
            resp.setStatus(HttpServletResponse.SC_OK);
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
        
        resp.getWriter().write(jsonResponse);
    }
}