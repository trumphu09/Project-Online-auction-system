package com.auction.server.servlets;

import com.auction.server.dao.BidderDAO; // Import BidderDAO
import com.google.gson.Gson;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors; // Cần để đọc JSON body

public class RegisterAPI extends HttpServlet {

    private final BidderDAO bidderDAO = new BidderDAO(); // Sử dụng BidderDAO
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

            String username = requestData.get("username");
            String password = requestData.get("password");
            String email = requestData.get("email");

            // --- FIX: Gọi bidderDAO.registerBidder để tạo cả user và bidder ---
            // Phương thức này đã xử lý role "BIDDER" (chữ hoa) bên trong
            boolean isSuccess = bidderDAO.registerBidder(username, password, email);

            if (isSuccess) {
                resp.setStatus(HttpServletResponse.SC_CREATED); // 201 Created là mã chuẩn cho tạo mới thành công
                responseMap.put("status", "success");
                responseMap.put("message", "Đăng ký tài khoản bidder thành công!");
            } else {
                // --- FIX: Set HTTP status code 409 Conflict cho lỗi trùng lặp ---
                resp.setStatus(HttpServletResponse.SC_CONFLICT); // 409 Conflict
                responseMap.put("status", "error");
                responseMap.put("message", "Đăng ký thất bại. Tên đăng nhập hoặc email có thể đã được sử dụng.");
            }

        } catch (Exception e) {
            System.err.println("Lỗi trong RegisterAPI: " + e.getMessage()); // Log lỗi nội bộ
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responseMap.put("status", "error");
            responseMap.put("message", "Đã có lỗi xảy ra phía server. Vui lòng thử lại sau."); // Thông báo lỗi chung
        } finally {
            resp.getWriter().write(gson.toJson(responseMap));
        }
    }
}