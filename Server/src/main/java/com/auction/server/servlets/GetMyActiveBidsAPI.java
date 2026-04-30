package com.auction.server.servlets;

import com.auction.controller.BidController;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class GetMyActiveBidsAPI extends HttpServlet {

    private final BidController bidController = new BidController();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        int userId = (Integer) session.getAttribute("userId");

        String jsonResponse = bidController.handleGetActiveBids(userId);
        
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(jsonResponse);
    }
}