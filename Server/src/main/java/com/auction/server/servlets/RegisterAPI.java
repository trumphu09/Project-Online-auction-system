package com.auction.server.servlets;

// THAY ĐỔI QUAN TRỌNG: Import BidderDAO thay vì UserDAO
import com.auction.server.dao.BidderDAO;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RegisterAPI extends HttpServlet {

    // THAY ĐỔI QUAN TRỌNG: Dùng BidderDAO làm "chuyên gia" chính
    private final BidderDAO bidderDAO = new BidderDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        // 1. Lấy thông tin đăng ký từ client
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String email = req.getParameter("email");

        // 2. GỌI ĐÚNG HÀM registerBidder(String...) MÀ BẠN ĐÃ VIẾT
        // Hàm này sẽ tự động xử lý việc tạo user và bidder một cách đồng bộ.
        boolean isSuccess = bidderDAO.registerBidder(username, password, email);

        Map<String, Object> responseMap = new HashMap<>();

        // 3. Dựa vào kết quả true/false để trả về thông báo
        if (isSuccess) {
            responseMap.put("status", "success");
            responseMap.put("message", "Đăng ký tài khoản bidder thành công!");
        } else {
            responseMap.put("status", "error");
            responseMap.put("message", "Đăng ký thất bại. Tên đăng nhập hoặc email có thể đã được sử dụng.");
        }

        // 4. Gửi JSON response về cho client
        resp.getWriter().write(gson.toJson(responseMap));
    }
}