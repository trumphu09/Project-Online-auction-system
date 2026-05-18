package com.auction.server.servlets;

import com.auction.controller.UserController;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.stream.Collectors;

public class SellerRatingAPI extends HttpServlet {

    private final UserController userController = new UserController();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"status\":\"error\",\"message\":\"Bạn cần đăng nhập.\"}");
            return;
        }

        String jsonRequest = req.getReader().lines()
                .collect(Collectors.joining(System.lineSeparator()));

        String jsonResponse = userController.handleRateSeller(jsonRequest);
        JsonObject obj = gson.fromJson(jsonResponse, JsonObject.class);

        if (obj != null && "success".equalsIgnoreCase(obj.get("status").getAsString())) {
            resp.setStatus(HttpServletResponse.SC_OK);
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        resp.getWriter().write(jsonResponse);
    }
}