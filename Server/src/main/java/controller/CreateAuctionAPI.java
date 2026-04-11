package controller;

import com.auction.server.dao.ItemDAO;
import com.auction.server.models.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

// ĐÉO CẦN DÙNG @WebServlet NỮA NHÉ! (Đã đăng ký trong AuctionServer)
public class CreateAuctionAPI extends HttpServlet {

    private final ItemDAO itemDAO;
    private final Gson gson;

    public CreateAuctionAPI() {
        this.itemDAO = new ItemDAO();
        this.gson = new Gson();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. Đọc JSON từ Frontend
            BufferedReader reader = request.getReader();
            JsonObject json = gson.fromJson(reader, JsonObject.class);

            if (json == null) {
                result.put("success", false);
                result.put("message", "Dữ liệu trống!");
                response.getWriter().write(gson.toJson(result));
                return;
            }

            // Bóc tách dữ liệu
            int itemId = json.has("itemId") ? json.get("itemId").getAsInt() : 0;
            int sellerId = json.has("sellerId") ? json.get("sellerId").getAsInt() : 0;
            String endTimeStr = json.has("endTime") ? json.get("endTime").getAsString() : null;

            if (itemId == 0 || sellerId == 0 || endTimeStr == null) {
                result.put("success", false);
                result.put("message", "Thiếu thông tin bắt buộc (itemId, sellerId, endTime)!");
                response.getWriter().write(gson.toJson(result));
                return;
            }

            // Ép kiểu thời gian
            LocalDateTime endTime = LocalDateTime.parse(endTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            // 2. Tìm món đồ trong Database
            // LƯU Ý: Mày cần phải có hàm getItemById trong file ItemDAO nhé!
            Item item = itemDAO.getItemById(itemId);
            if (item == null) {
                result.put("success", false);
                result.put("message", "Không tìm thấy món đồ với ID: " + itemId);
                response.getWriter().write(gson.toJson(result));
                return;
            }

            // 3. Khởi tạo người bán (Seller)
            // Tạm thời tao tạo một Object Seller ảo. Nếu hệ thống của mày có UserDAO
            // 4. Sinh ID cho phiên đấu giá (Lấy số lượng hiện tại + 1)
            int newAuctionId = AuctionManager.getInstance().getAllAuctions().size() + 1;

            // 5. Tạo Phiên đấu giá mới
            User seller = null;
            Auction newAuction = new Auction(newAuctionId, item, seller, endTime);

            // 6. Quăng nó lên sàn (Lưu vào RAM qua AuctionManager)
            AuctionManager.getInstance().createAuction(newAuction);

            // Trả về thành công
            result.put("success", true);
            result.put("message", "Đã mở phiên đấu giá thành công!");
            result.put("auctionId", newAuction.getId());

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Lỗi server: " + e.getMessage());
            e.printStackTrace();
        }

        response.getWriter().write(gson.toJson(result));
    }
}