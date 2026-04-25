package com.auction.server.servlets;

import com.auction.server.dao.BidsDAO;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession; // Thêm import này
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class PlaceBidAPI extends HttpServlet {

    private final BidsDAO bidsDAO = new BidsDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        Map<String, Object> responseMap = new HashMap<>();

        try {
            // Lấy userId từ session
            HttpSession session = req.getSession(false); // Không tạo session mới nếu chưa có
            Integer userId = null;
            if (session != null) {
                userId = (Integer) session.getAttribute("userId");
            }

            if (userId == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
                responseMap.put("status", "error");
                responseMap.put("message", "Bạn cần đăng nhập để đặt giá.");
                resp.getWriter().write(gson.toJson(responseMap));
                return;
            }

            String requestBody = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            JsonObject bidRequest = gson.fromJson(requestBody, JsonObject.class);

            int itemId = bidRequest.get("itemId").getAsInt();
            double bidAmount = bidRequest.get("bidAmount").getAsDouble();

            // Sử dụng userId an toàn từ session
            boolean isSuccess = bidsDAO.executeBid(itemId, userId, bidAmount);

            if (isSuccess) {
                resp.setStatus(HttpServletResponse.SC_OK);
                responseMap.put("status", "success");
                responseMap.put("message", "Đặt giá thành công!");
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400 Bad Request
                responseMap.put("status", "error");
                responseMap.put("message", "Đặt giá không thành công. Vui lòng kiểm tra lại giá, số dư hoặc trạng thái sản phẩm.");
            }

        } catch (NumberFormatException e) {
            System.err.println("Lỗi định dạng số trong PlaceBidAPI: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            responseMap.put("status", "error");
            responseMap.put("message", "Dữ liệu đặt giá không hợp lệ (itemId hoặc bidAmount).");
        } catch (Exception e) {
            System.err.println("Lỗi trong PlaceBidAPI: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responseMap.put("status", "error");
            responseMap.put("message", "Đã có lỗi xảy ra phía server. Vui lòng thử lại sau.");
        } finally {
            resp.getWriter().write(gson.toJson(responseMap));
        }
    }
}