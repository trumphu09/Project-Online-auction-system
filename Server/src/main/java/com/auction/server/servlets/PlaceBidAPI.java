package com.auction.server.servlets;

import com.auction.controller.AuctionController;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.stream.Collectors;

public class PlaceBidAPI extends HttpServlet {

    private final AuctionController auctionController = new AuctionController();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        int userId = (Integer) session.getAttribute("userId");

        String jsonRequest = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        
        String jsonResponse = auctionController.handlePlaceBid(jsonRequest, userId);

        JsonObject respObj = gson.fromJson(jsonResponse, JsonObject.class);
        if (respObj != null && "success".equals(respObj.get("status").getAsString())) {
            resp.setStatus(HttpServletResponse.SC_OK);
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        resp.getWriter().write(jsonResponse);
    }
}