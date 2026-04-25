package com.auction.server.servlets;

import com.auction.server.dao.UserDAO;
import com.google.gson.Gson;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession; // Thêm import này
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class LoginAPI extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        Map<String, Object> responseMap = new HashMap<>();

        try {
            String requestBody = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            Map<String, String> requestData = gson.fromJson(requestBody, Map.class);

            String email = requestData.get("email");
            String password = requestData.get("password");

            // Gọi phương thức loginUser mới, trả về Map
            Map<String, Object> userDetails = userDAO.loginUser(email, password);

            if (userDetails != null) {
                String userRole = (String) userDetails.get("role");
                Integer userId = (Integer) userDetails.get("userId");

                if ("INACTIVE".equals(userRole)) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    responseMap.put("status", "error");
                    responseMap.put("message", "Tài khoản của bạn đã bị khóa.");
                } else {
                    // --- TẠO SESSION VÀ LƯU THÔNG TIN ---
                    // req.getSession(true) sẽ tạo session mới nếu chưa có
                    HttpSession session = req.getSession(true); 
                    session.setAttribute("userId", userId);
                    session.setAttribute("userRole", userRole);
                    
                    // Đặt thời gian timeout cho session (ví dụ: 30 phút)
                    session.setMaxInactiveInterval(30 * 60); 

                    resp.setStatus(HttpServletResponse.SC_OK);
                    responseMap.put("status", "success");
                    responseMap.put("message", "Đăng nhập thành công!");
                    // Trả về thông tin user để client có thể sử dụng
                    responseMap.put("userId", userId); 
                    responseMap.put("role", userRole);
                }
            } else {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                responseMap.put("status", "error");
                responseMap.put("message", "Email hoặc mật khẩu không đúng.");
            }

        } catch (Exception e) {
            System.err.println("Lỗi trong LoginAPI: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responseMap.put("status", "error");
            responseMap.put("message", "Đã có lỗi xảy ra phía server. Vui lòng thử lại sau.");
        } finally {
            resp.getWriter().write(gson.toJson(responseMap));
        }
    }
}