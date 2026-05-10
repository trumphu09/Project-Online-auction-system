package com.auction.server.servlets;

import com.auction.controller.UserController;
import com.auction.server.dao.UserDAO;
import com.auction.server.models.UserDTO;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/api/users/*")
public class UserDetailAPI extends HttpServlet {
    private final UserDAO userDAO = new UserDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            sendResponse(resp, false, "Thiếu ID người dùng", null);
            return;
        }

        try {
            // Extract userId từ URL: /api/users/{userId}
            String userIdStr = pathInfo.substring(1); // Remove leading /
            int userId = Integer.parseInt(userIdStr);

            UserDTO user = userDAO.getUserById(userId);

            if (user != null) {
                sendResponse(resp, true, "Lấy thông tin người dùng thành công", user);
            } else {
                sendResponse(resp, false, "Không tìm thấy người dùng", null);
            }
        } catch (NumberFormatException e) {
            sendResponse(resp, false, "ID người dùng không hợp lệ", null);
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(resp, false, "Lỗi hệ thống: " + e.getMessage(), null);
        }
    }

    private void sendResponse(HttpServletResponse resp, boolean success, String message, Object data) throws IOException {
        resp.setContentType("application/json");
        JsonObject response = new JsonObject();
        response.addProperty("success", success);
        response.addProperty("message", message);
        if (success && data != null) {
            response.add("data", gson.toJsonTree(data));
        }
        resp.getWriter().print(response.toString());
    }
}
