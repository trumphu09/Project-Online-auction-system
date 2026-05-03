package com.auction.server.servlets;

import com.google.gson.Gson;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LogoutAPI extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        Map<String, Object> responseMap = new HashMap<>();

        try {
            HttpSession session = req.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            
            // Luôn trả về 200 OK, vì mục tiêu là đảm bảo client ở trạng thái đã đăng xuất.
            resp.setStatus(HttpServletResponse.SC_OK);
            responseMap.put("status", "success");
            responseMap.put("message", "Đã đăng xuất thành công.");

        } catch (Exception e) {
            System.err.println("Lỗi trong LogoutAPI: " + e.getMessage());
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responseMap.put("status", "error");
            responseMap.put("message", "Đã có lỗi xảy ra ở phía máy chủ.");
        } finally {
            resp.getWriter().write(gson.toJson(responseMap));
        }
    }
}