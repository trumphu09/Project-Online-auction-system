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

public class LoginAPI extends HttpServlet {

    private final UserController userController = new UserController();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String jsonRequest = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        String jsonResponse = userController.handleLogin(jsonRequest);

        JsonObject respObj = gson.fromJson(jsonResponse, JsonObject.class);
        if (respObj != null && respObj.has("status") && "success".equals(respObj.get("status").getAsString())) {
            JsonObject data = respObj.has("data") ? respObj.getAsJsonObject("data") : null;
            if (data != null && data.has("userId")) {
                int userId = data.get("userId").getAsInt();
                String userRole = data.has("role") ? data.get("role").getAsString() : null;
                
                HttpSession session = req.getSession(true);
                session.setAttribute("userId", userId);
                session.setAttribute("userRole", userRole);
                session.setMaxInactiveInterval(30 * 60);
                
                resp.setStatus(HttpServletResponse.SC_OK);
            } else {
                // Lỗi logic: Controller trả về success nhưng không có data
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }

        resp.getWriter().write(jsonResponse);
    }
}