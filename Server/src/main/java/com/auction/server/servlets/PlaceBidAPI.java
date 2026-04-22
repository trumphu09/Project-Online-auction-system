package com.auction.server.servlets;

import com.auction.server.dao.BidsDAO;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;

public class PlaceBidAPI extends HttpServlet {

    private final BidsDAO bidsDAO = new BidsDAO();
    private final Gson gson = new Gson();

    /**
     * Xử lý yêu cầu POST để đặt giá cho một sản phẩm.
     * API này sẽ gọi thẳng đến phương thức executeBid đã có sẵn trong BidsDAO.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            // 1. Đọc chuỗi JSON từ body của request
            String requestBody = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            JsonObject bidRequest = gson.fromJson(requestBody, JsonObject.class);

            int itemId = bidRequest.get("itemId").getAsInt();
            double bidAmount = bidRequest.get("bidAmount").getAsDouble();
            int userId = bidRequest.get("userId").getAsInt();

            // 2. Gọi thẳng đến phương thức executeBid của bạn.
            // Mọi logic phức tạp đã được xử lý bên trong phương thức này.
            boolean isSuccess = bidsDAO.executeBid(itemId, userId, bidAmount);

            // 3. Dựa vào kết quả true/false để trả về thông báo
            if (isSuccess) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write("{\"status\":\"success\", \"message\":\"Đặt giá thành công!\"}");
            } else {
                // executeBid trả về false có thể do nhiều nguyên nhân:
                // - Giá đặt thấp
                // - Sản phẩm đã kết thúc
                // - Người dùng không đủ tiền
                // - ...
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"status\":\"error\", \"message\":\"Đặt giá không thành công. Vui lòng kiểm tra lại giá hoặc số dư.\"}");
            }

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"status\":\"error\", \"message\":\"Đã có lỗi xảy ra phía server: " + e.getMessage() + "\"}");
        }
    }
}