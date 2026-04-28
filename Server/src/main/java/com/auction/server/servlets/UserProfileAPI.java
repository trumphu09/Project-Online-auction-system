package com.auction.server.servlets;

import com.auction.server.dao.UserDAO;
import com.auction.server.models.UserDTO;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class UserProfileAPI extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        Map<String, Object> responseMap = new HashMap<>();

        try {
            HttpSession session = req.getSession(false);
            // Filter đã đảm bảo session và userId tồn tại
            int userId = (Integer) session.getAttribute("userId");
            UserDTO user = userDAO.getUserById(userId);

            if (user != null) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write(gson.toJson(user));
            } else {
                // Trường hợp hiếm gặp: user bị xóa sau khi login
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                responseMap.put("status", "error");
                responseMap.put("message", "Không tìm thấy thông tin người dùng.");
                resp.getWriter().write(gson.toJson(responseMap));
            }
        } catch (Exception e) {
            System.err.println("Lỗi không xác định trong UserProfileAPI (GET): " + e.getMessage());
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responseMap.put("status", "error");
            responseMap.put("message", "Đã có lỗi xảy ra ở phía máy chủ.");
            resp.getWriter().write(gson.toJson(responseMap));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        Map<String, Object> responseMap = new HashMap<>();

        try {
            HttpSession session = req.getSession(false);
            // Filter đã đảm bảo session và userId tồn tại
            int userId = (Integer) session.getAttribute("userId");

            JsonObject requestJson;
            try {
                String requestBody = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
                requestJson = gson.fromJson(requestBody, JsonObject.class);
                if (requestJson == null || !requestJson.has("oldPassword") || !requestJson.has("newPassword")) {
                    throw new JsonSyntaxException("Request body must contain 'oldPassword' and 'newPassword'.");
                }
            } catch (JsonSyntaxException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                responseMap.put("status", "error");
                responseMap.put("message", "Dữ liệu JSON không hợp lệ hoặc thiếu trường.");
                resp.getWriter().write(gson.toJson(responseMap));
                return;
            }

            String oldPassword = requestJson.get("oldPassword").getAsString();
            String newPassword = requestJson.get("newPassword").getAsString();

            if (oldPassword.isEmpty() || newPassword.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                responseMap.put("status", "error");
                responseMap.put("message", "Mật khẩu cũ và mới không được để trống.");
                resp.getWriter().write(gson.toJson(responseMap));
                return;
            }

            boolean isSuccess = userDAO.changePassword(userId, oldPassword, newPassword);

            if (isSuccess) {
                responseMap.put("status", "success");
                responseMap.put("message", "Đổi mật khẩu thành công.");
                resp.setStatus(HttpServletResponse.SC_OK);
            } else {
                responseMap.put("status", "error");
                responseMap.put("message", "Đổi mật khẩu thất bại. Vui lòng kiểm tra lại mật khẩu cũ.");
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (Exception e) {
            System.err.println("Lỗi không xác định trong UserProfileAPI (PUT): " + e.getMessage());
            e.printStackTrace();
            responseMap.put("status", "error");
            responseMap.put("message", "Đã có lỗi xảy ra ở phía máy chủ.");
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            resp.getWriter().write(gson.toJson(responseMap));
        }
    }
}