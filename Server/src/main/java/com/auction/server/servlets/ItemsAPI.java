package com.auction.server.servlets;

import com.auction.server.dao.ItemDAO;
import com.auction.server.models.Item;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * API này xử lý tất cả các tác vụ liên quan đến danh sách sản phẩm.
 * - GET /api/items: Lấy danh sách tất cả sản phẩm.
 * - POST /api/items: Tạo một sản phẩm mới.
 */
public class ItemsAPI extends HttpServlet {

    private final ItemDAO itemDAO = new ItemDAO();
    private final Gson gson = new Gson();

    /**
     * Xử lý yêu cầu GET để lấy danh sách tất cả các sản phẩm.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        // Gọi đúng hàm getAllItems() đã có sẵn trong ItemDAO
        List<Item> items = itemDAO.getAllItems();
        String itemsJson = gson.toJson(items);
        resp.getWriter().write(itemsJson);
    }

    /**
     * Xử lý yêu cầu POST để tạo một sản phẩm mới.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            // 1. Đọc chuỗi JSON từ body của request
            String requestBody = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

            // 2. Dùng Gson để dịch chuỗi JSON thành đối tượng Item
            Item newItem = gson.fromJson(requestBody, Item.class);

            // 3. Gọi đúng hàm addItem(Item item) trong ItemDAO để lưu vào database
            boolean isSuccess = itemDAO.addItem(newItem);

            if (isSuccess) {
                // 4. Nếu thành công, trả về status 201 và thông tin sản phẩm vừa tạo
                resp.setStatus(HttpServletResponse.SC_CREATED); // 201 Created là mã chuẩn
                // Hàm addItem đã cập nhật ID vào object newItem, nên ta có thể lấy ra dùng
                resp.getWriter().write("{\"status\":\"success\", \"message\":\"Đăng bán sản phẩm thành công!\", \"itemId\":" + newItem.getId() + "}");
            } else {
                throw new Exception("Không thể lưu sản phẩm vào database.");
            }

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"status\":\"error\", \"message\":\"Lỗi phía server: " + e.getMessage() + "\"}");
        }
    }
}