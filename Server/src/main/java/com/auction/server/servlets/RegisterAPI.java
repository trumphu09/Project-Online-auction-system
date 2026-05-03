package com.auction.server.servlets;

import com.auction.controller.UserController;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;

public class RegisterAPI extends HttpServlet {

    private final UserController userController = new UserController();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String jsonRequest = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        String jsonResponse = userController.handleRegister(jsonRequest);

        if (jsonResponse.contains("\"status\":\"success\"")) {
            resp.setStatus(HttpServletResponse.SC_CREATED);
        } else {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
        }

        resp.getWriter().write(jsonResponse);
    }
}