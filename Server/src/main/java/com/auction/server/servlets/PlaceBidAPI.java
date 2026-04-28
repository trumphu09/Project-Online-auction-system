package com.auction.server.servlets;

import com.auction.server.AuctionServer;
import com.auction.server.dao.BidsDAO;
import com.auction.server.dao.UserDAO;
import com.auction.server.models.UserDTO;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class PlaceBidAPI extends HttpServlet {

    private final BidsDAO bidsDAO = new BidsDAO();
    private final UserDAO userDAO = new UserDAO(); // Thêm UserDAO
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        Map<String, Object> responseMap = new HashMap<>();

        try {
            HttpSession session = req.getSession(false);
            int userId = (Integer) session.getAttribute("userId");

            JsonObject bidRequest;
            try {
                String requestBody = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
                bidRequest = gson.fromJson(requestBody, JsonObject.class);
                if (bidRequest == null || !bidRequest.has("itemId") || !bidRequest.has("bidAmount")) {
                    throw new JsonSyntaxException("Request body must contain 'itemId' and 'bidAmount'.");
                }
            } catch (JsonSyntaxException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                responseMap.put("status", "error");
                responseMap.put("message", "Dữ liệu JSON không hợp lệ hoặc thiếu trường.");
                resp.getWriter().write(gson.toJson(responseMap));
                return;
            }

            int itemId = bidRequest.get("itemId").getAsInt();
            double bidAmount = bidRequest.get("bidAmount").getAsDouble();

            boolean isSuccess = bidsDAO.executeBid(itemId, userId, bidAmount);

            if (isSuccess) {
                responseMap.put("status", "success");
                responseMap.put("message", "Đặt giá thành công!");
                resp.setStatus(HttpServletResponse.SC_OK);

                // Lấy username để thông báo
                UserDTO bidder = userDAO.getUserById(userId);
                String bidderUsername = (bidder != null) ? bidder.getUsername() : "Một người dùng";

                // Thông báo cho WebSocket server
                AuctionServer.getWebSocketServer().broadcastNewBid(itemId, bidAmount, bidderUsername);

            } else {
                responseMap.put("status", "error");
                responseMap.put("message", "Đặt giá không thành công. Vui lòng kiểm tra lại giá, số dư hoặc trạng thái sản phẩm.");
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (Exception e) {
            System.err.println("Lỗi không xác định trong PlaceBidAPI: " + e.getMessage());
            e.printStackTrace();
            responseMap.put("status", "error");
            responseMap.put("message", "Đã có lỗi xảy ra ở phía máy chủ.");
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            resp.getWriter().write(gson.toJson(responseMap));
        }
    }
}