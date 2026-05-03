package com.auction.server.servlets;

import com.auction.controller.UserController;
import com.google.gson.Gson;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;

public class AdminUserAPI extends HttpServlet {

    private final UserController userController = new UserController();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        
        String jsonResponse = userController.handleGetAllUsers();
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(jsonResponse);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();
        String[] pathParts = (pathInfo != null) ? pathInfo.split("/") : new String[0];

        if (pathParts.length != 2) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"URL không hợp lệ. Định dạng đúng: /api/admin/users/{userId}\"}");
            return;
        }

        try {
            int userIdToUpdate = Integer.parseInt(pathParts[1]);
            String requestBody = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            
            String jsonResponse = userController.handleUpdateUserRole(userIdToUpdate, requestBody);

            if (jsonResponse.contains("\"status\":\"success\"")) {
                resp.setStatus(HttpServletResponse.SC_OK);
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
            resp.getWriter().write(jsonResponse);

        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"ID người dùng không hợp lệ.\"}");
        }
    }
}