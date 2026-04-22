package com.auction.server.servlets;

import com.auction.server.dao.UserDAO;
import com.google.gson.Gson;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors; // Cần để đọc JSON body

public class LoginAPI extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        Map<String, Object> responseMap = new HashMap<>();

        try {
            // --- FIX: Đọc JSON body thay vì form-urlencoded để đồng bộ ---
            String requestBody = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            Map<String, String> requestData = gson.fromJson(requestBody, Map.class);

            String email = requestData.get("email");
            String password = requestData.get("password");

            String userRole = userDAO.loginUser(email, password);

            if (userRole != null) {
                if ("INACTIVE".equals(userRole)) {
                    // --- FIX: Set HTTP status code 403 Forbidden cho user inactive ---
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403 Forbidden
                    responseMap.put("status", "error");
                    responseMap.put("message", "Tài khoản của bạn đã bị khóa.");
                } else {
                    resp.setStatus(HttpServletResponse.SC_OK); // 200 OK
                    responseMap.put("status", "success");
                    responseMap.put("message", "Đăng nhập thành công!");
                    responseMap.put("role", userRole);
                }
            } else {
                // --- FIX: Set HTTP status code 401 Unauthorized cho sai email/password ---
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
                responseMap.put("status", "error");
                responseMap.put("message", "Email hoặc mật khẩu không đúng.");
            }

        } catch (Exception e) {
            System.err.println("Lỗi trong LoginAPI: " + e.getMessage()); // Log lỗi nội bộ
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responseMap.put("status", "error");
            responseMap.put("message", "Đã có lỗi xảy ra phía server. Vui lòng thử lại sau."); // Thông báo lỗi chung
        } finally {
            resp.getWriter().write(gson.toJson(responseMap));
        }
    }
}