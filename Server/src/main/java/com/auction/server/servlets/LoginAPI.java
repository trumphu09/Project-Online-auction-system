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

public class LoginAPI extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 1. Lấy email và password từ client
        String email = req.getParameter("email");
        String password = req.getParameter("password");

        // 2. Gọi đúng hàm loginUser từ UserDAO của bạn
        String userRole = userDAO.loginUser(email, password);

        Map<String, Object> responseMap = new HashMap<>();

        // 3. Kiểm tra kết quả: Nếu userRole không phải null, tức là đăng nhập thành công
        if (userRole != null) {
            responseMap.put("status", "success");
            responseMap.put("message", "Đăng nhập thành công!");
            responseMap.put("role", userRole); // Gửi vai trò về cho client
        } else {
            responseMap.put("status", "error");
            responseMap.put("message", "Email hoặc mật khẩu không đúng.");
        }

        // 4. Gửi JSON response về cho client
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(gson.toJson(responseMap));
    }
}