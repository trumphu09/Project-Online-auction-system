package controller;

import com.auction.server.dao.ItemDAO;
import com.auction.server.models.Item;
import com.auction.server.models.Art;
import com.auction.server.models.Electronics;
import com.auction.server.models.Vehicle;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/items/create")
public class CreateItemAPI extends HttpServlet {

    private final ItemDAO itemDAO;
    private final Gson gson;

    public CreateItemAPI() {
        this.itemDAO = new ItemDAO();
        this.gson = new Gson();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 1. Cấu hình định dạng JSON và tiếng Việt
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        Map<String, Object> result = new HashMap<>();

        try {
            // 2. Đọc JSON từ Frontend gửi lên
            BufferedReader reader = request.getReader();
            JsonObject json = gson.fromJson(reader, JsonObject.class);

            if (json == null) {
                result.put("success", false);
                result.put("message", "Dữ liệu trống!");
                response.getWriter().write(gson.toJson(result));
                return;
            }

            // 3. Bóc tách dữ liệu
            String name = json.has("name") ? json.get("name").getAsString() : "";
            String description = json.has("description") ? json.get("description").getAsString() : "";
            double startingPrice = json.has("startingPrice") ? json.get("startingPrice").getAsDouble() : 0;
            int sellerId = json.has("sellerId") ? json.get("sellerId").getAsInt() : 0;
            String category = json.has("category") ? json.get("category").getAsString().toUpperCase() : "";

            String startTimeStr = json.has("startTime") ? json.get("startTime").getAsString() : null;
            String endTimeStr = json.has("endTime") ? json.get("endTime").getAsString() : null;

            // Validate cơ bản
            if (name.isEmpty() || startingPrice <= 0 || sellerId == 0 || category.isEmpty() || startTimeStr == null || endTimeStr == null) {
                result.put("success", false);
                result.put("message", "Thiếu thông tin bắt buộc!");
                response.getWriter().write(gson.toJson(result));
                return;
            }

            // Chuyển chuỗi thời gian sang LocalDateTime
            LocalDateTime startTime = LocalDateTime.parse(startTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            LocalDateTime endTime = LocalDateTime.parse(endTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            // 4. FACTORY PATTERN: Khởi tạo lớp con tương ứng
            Item newItem = null;
            int defaultId = 0; // ID sẽ do CSDL tự sinh

            switch (category) {
                case "ART":
                    newItem = new Art(defaultId, sellerId, name, description, startingPrice, startTime, endTime, category);
                    break;
                case "ELECTRONICS":
                    newItem = new Electronics(defaultId, sellerId, name, description, startingPrice, startTime, endTime, category);
                    break;
                case "VEHICLE":
                    newItem = new Vehicle(defaultId, sellerId, name, description, startingPrice, startTime, endTime, category);
                    break;
                default:
                    result.put("success", false);
                    result.put("message", "Danh mục (Category) không hợp lệ!");
                    response.getWriter().write(gson.toJson(result));
                    return;
            }

            // 5. Gọi DAO để lưu vào Database (gọi đúng hàm addItem của bạn mày)
            boolean isInserted = itemDAO.addItem(newItem);

            if (isInserted) {
                result.put("success", true);
                result.put("message", "Thêm sản phẩm thành công!");
                // Nếu DAO chạy chuẩn, newItem.getId() sẽ chứa ID vừa sinh ra từ DB
                result.put("itemId", newItem.getId());
            } else {
                result.put("success", false);
                result.put("message", "Lỗi CSDL: Không thể lưu sản phẩm!");
            }

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Lỗi server: " + e.getMessage());
            e.printStackTrace();
        }

        // 6. Trả JSON về
        response.getWriter().write(gson.toJson(result));
    }
}