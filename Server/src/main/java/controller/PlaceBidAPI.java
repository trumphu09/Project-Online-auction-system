package controller;

import com.auction.server.models.AuctionManager;
import com.auction.server.models.Bidder;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PlaceBidAPI extends HttpServlet {
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. Đọc dữ liệu JSON đặt giá từ người dùng
            BufferedReader reader = request.getReader();
            JsonObject json = gson.fromJson(reader, JsonObject.class);

            if (json == null) {
                result.put("success", false);
                result.put("message", "Dữ liệu đặt giá trống!");
                response.getWriter().write(gson.toJson(result));
                return;
            }

            int auctionId = json.get("auctionId").getAsInt();
            int bidderId = json.get("bidderId").getAsInt();
            double bidAmount = json.get("amount").getAsDouble();

            // 2. Tạo đối tượng Bidder (Người đặt giá)
            // LƯU Ý: Tạm thời tao fake số dư 1 tỷ cho sướng.
            // Sau này mày có thể dùng UserDAO để lấy số dư thật từ DB.
            Bidder bidder = new Bidder(bidderId, "User_" + bidderId, "bidder@uet.vnu.edu.vn", "123", 1000000000.0);

            // 3. Gửi yêu cầu đặt giá vào Manager
            // AuctionManager sẽ xử lý bất đồng bộ (Async) để tránh nghẽn mạng
            boolean isSent = AuctionManager.getInstance().processBidRequest(auctionId, bidder, bidAmount);

            if (isSent) {
                result.put("success", true);
                result.put("message", "Yêu cầu đặt giá của bạn đang được xử lý!");
            } else {
                result.put("success", false);
                result.put("message", "Lỗi: Không tìm thấy phiên đấu giá này!");
            }

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Lỗi hệ thống: " + e.getMessage());
            e.printStackTrace();
        }

        // Trả kết quả về cho Frontend/Postman
        response.getWriter().write(gson.toJson(result));
    }
}