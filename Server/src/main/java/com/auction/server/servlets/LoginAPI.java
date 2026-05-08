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
        
        // 1. Lấy kết quả từ Controller (Nó ĐÃ CÓ SẴN vỏ "data" rồi: {"status":"success", "data":{"userId":1, "role":"ADMIN"}})
        String jsonResponse = userController.handleLogin(jsonRequest);
        JsonObject respObj = gson.fromJson(jsonResponse, JsonObject.class);

        // 2. Kiểm tra nếu đăng nhập thành công
        if (respObj != null && respObj.has("status") && "success".equals(respObj.get("status").getAsString())) {
            
            // Lấy cái lõi "data" ra trước
            JsonObject dataObj = respObj.getAsJsonObject("data");
            
            // Đọc thông tin từ bên trong lõi "data"
            String userRoleStr = dataObj.has("role") ? dataObj.get("role").getAsString() : null;
            int userId = dataObj.has("userId") ? dataObj.get("userId").getAsInt() : 0;
            
            // 3. Lưu vào Session của Server
            HttpSession session = req.getSession(true);
            session.setAttribute("userId", userId);
            session.setAttribute("userRole", com.auction.server.models.UserRole.fromString(userRoleStr));
            session.setMaxInactiveInterval(30 * 60);

            // 4. Trả về đúng cái JSON gốc của Controller (vì nó đã chuẩn 100% rồi)
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(jsonResponse);
            
        } else {
            // Đăng nhập thất bại (sai email/pass), trả về nguyên trạng thông báo lỗi
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write(jsonResponse);
        }
    }
}