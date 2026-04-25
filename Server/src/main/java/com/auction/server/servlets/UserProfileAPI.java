package com.auction.server.servlets;

import com.auction.server.dao.UserDAO;
import com.auction.server.models.UserDTO;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * API để quản lý hồ sơ người dùng.
 * - GET /api/my/profile: Lấy thông tin hồ sơ.
 * - PUT /api/my/profile/password: Đổi mật khẩu.
 */
public class UserProfileAPI extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final Gson gson = new Gson();

    // Lấy thông tin hồ sơ
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        Map<String, Object> responseMap = new HashMap<>();

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            responseMap.put("status", "error");
            responseMap.put("message", "Bạn cần đăng nhập để xem hồ sơ.");
            resp.getWriter().write(gson.toJson(responseMap));
            return;
        }

        try {
            int userId = (Integer) session.getAttribute("userId");
            UserDTO user = userDAO.getUserById(userId);

            if (user != null) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write(gson.toJson(user));
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                responseMap.put("status", "error");
                responseMap.put("message", "Không tìm thấy thông tin người dùng.");
                resp.getWriter().write(gson.toJson(responseMap));
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responseMap.put("status", "error");
            responseMap.put("message", "Lỗi phía server.");
            resp.getWriter().write(gson.toJson(responseMap));
            e.printStackTrace();
        }
    }

    // Đổi mật khẩu
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        Map<String, Object> responseMap = new HashMap<>();

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            responseMap.put("status", "error");
            responseMap.put("message", "Bạn cần đăng nhập để đổi mật khẩu.");
            resp.getWriter().write(gson.toJson(responseMap));
            return;
        }

        try {
            int userId = (Integer) session.getAttribute("userId");

            String requestBody = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            JsonObject requestJson = gson.fromJson(requestBody, JsonObject.class);

            String oldPassword = requestJson.get("oldPassword").getAsString();
            String newPassword = requestJson.get("newPassword").getAsString();

            if (oldPassword == null || newPassword == null || newPassword.trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                responseMap.put("status", "error");
                responseMap.put("message", "Mật khẩu cũ và mới không được để trống.");
                resp.getWriter().write(gson.toJson(responseMap));
                return;
            }

            boolean isSuccess = userDAO.changePassword(userId, oldPassword, newPassword);

            if (isSuccess) {
                resp.setStatus(HttpServletResponse.SC_OK);
                responseMap.put("status", "success");
                responseMap.put("message", "Đổi mật khẩu thành công.");
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                responseMap.put("status", "error");
                responseMap.put("message", "Đổi mật khẩu thất bại. Vui lòng kiểm tra lại mật khẩu cũ.");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responseMap.put("status", "error");
            responseMap.put("message", "Lỗi phía server.");
            e.printStackTrace();
        } finally {
            resp.getWriter().write(gson.toJson(responseMap));
        }
    }
}