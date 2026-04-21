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

public class RegisterAPI extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 1. Lấy thông tin đăng ký từ client
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String email = req.getParameter("email");
        String role = "bidder"; // Mặc định vai trò là bidder

        // 2. Gọi thẳng hàm registerUser đã có sẵn trong UserDAO của bạn.
        // Hàm này sẽ trả về false nếu username/email bị trùng.
        boolean isSuccess = userDAO.registerUser(username, password, email, role);

        Map<String, Object> responseMap = new HashMap<>();

        // 3. Dựa vào kết quả true/false để trả về thông báo
        if (isSuccess) {
            responseMap.put("status", "success");
            responseMap.put("message", "Đăng ký thành công!");
        } else {
            responseMap.put("status", "error");
            responseMap.put("message", "Đăng ký thất bại. Tên đăng nhập hoặc email có thể đã được sử dụng.");
        }

        // 4. Gửi JSON response về cho client
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(gson.toJson(responseMap));
    }
}