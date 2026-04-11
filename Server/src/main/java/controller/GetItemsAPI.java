package controller;

import com.auction.server.dao.ItemDAO;
import com.auction.server.models.Item;
import com.google.gson.Gson;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Biển tên phòng: /api/items (Không có chữ create, hiểu ngầm là gọi để lấy list)
@WebServlet("/api/items")
public class GetItemsAPI extends HttpServlet {

    private final ItemDAO itemDAO;
    private final Gson gson;

    public GetItemsAPI() {
        this.itemDAO = new ItemDAO();
        this.gson = new Gson();
    }

    // Dùng doGet vì API này chỉ lấy dữ liệu ra, không giấu giếm gì cả
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 1. Cấu hình định dạng JSON và tiếng Việt
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        Map<String, Object> result = new HashMap<>();

        try {
            // 2. Gọi DAO chọc xuống DB lấy danh sách
            List<Item> items = itemDAO.getAllItems();

            // 3. Đóng gói kết quả
            if (items != null) {
                result.put("success", true);
                result.put("message", "Lấy danh sách thành công!");
                result.put("total", items.size()); // Báo cho FE biết có bao nhiêu món
                result.put("data", items);         // Nhét nguyên cái List Java vào đây
            } else {
                result.put("success", false);
                result.put("message", "Không thể lấy danh sách sản phẩm!");
            }

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Lỗi server: " + e.getMessage());
            e.printStackTrace();
        }

        // 4. Sự vi diệu của Gson: Tự động biến List<Item> thành chuỗi mảng JSON [...]
        response.getWriter().write(gson.toJson(result));
    }
}