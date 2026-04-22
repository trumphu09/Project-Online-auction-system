package com.auction.server.servlets;

import com.auction.server.dao.BidsDAO;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
            String requestBody = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            JsonObject bidRequest = gson.fromJson(requestBody, JsonObject.class);

            int itemId = bidRequest.get("itemId").getAsInt();
            double bidAmount = bidRequest.get("bidAmount").getAsDouble();

            // --- CRITICAL FIX: DO NOT TRUST userId FROM REQUEST BODY ---
            // userId MUST come from a secure session after authentication.
            // For now, we'll use a hardcoded placeholder for compilation/testing.
            // In a real application, this would be:
            // Integer userId = (Integer) req.getSession().getAttribute("userId");
            // if (userId == null) {
            //     resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
            //     responseMap.put("status", "error");
            //     responseMap.put("message", "Bạn cần đăng nhập để đặt giá.");
            //     return;
            // }
            int secureUserId = 1; // TODO: THAY THẾ BẰNG userId TỪ SESSION AN TOÀN SAU KHI ĐĂNG NHẬP!
            // em Thiện tạm thời hashcode tí nhé, nao em Thiện sửa nha.

            // --- FIX: Sử dụng thông báo lỗi chung, không lộ chi tiết nội bộ ---
            boolean isSuccess = bidsDAO.executeBid(itemId, secureUserId, bidAmount);

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
            System.err.println("Lỗi định dạng số trong PlaceBidAPI: " + e.getMessage()); // Log lỗi nội bộ
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            responseMap.put("status", "error");
            responseMap.put("message", "Dữ liệu đặt giá không hợp lệ (itemId hoặc bidAmount).");
        } catch (Exception e) {
            System.err.println("Lỗi trong PlaceBidAPI: " + e.getMessage()); // Log lỗi nội bộ
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responseMap.put("status", "error");
            responseMap.put("message", "Đã có lỗi xảy ra phía server. Vui lòng thử lại sau.");
        } finally {
            resp.getWriter().write(gson.toJson(responseMap));
        }
    }
}