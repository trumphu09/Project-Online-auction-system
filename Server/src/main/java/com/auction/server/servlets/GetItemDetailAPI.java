package com.auction.server.servlets;
//lấy item cụ thể (phải có id mới xem đc cụ thể nha) nó sẽ trả về 1 item cụ thể
import com.auction.server.dao.ItemDAO;
import com.auction.server.models.Item;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GetItemDetailAPI extends HttpServlet {

    private final ItemDAO itemDAO = new ItemDAO();
    private final Gson gson = new Gson();

    /**
     * Xử lý yêu cầu GET để lấy thông tin chi tiết của MỘT sản phẩm dựa trên ID.
     * URL ví dụ: /api/items/1
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        // 1. Lấy phần path phía sau "/api/items/". Ví dụ: "/1"
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            // Trường hợp người dùng chỉ gọi /api/items/ mà không có ID
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Thiếu ID của sản phẩm.\"}");
            return;
        }

        try {
            // 2. Bỏ dấu "/" ở đầu và chuyển chuỗi ID thành số nguyên
            int itemId = Integer.parseInt(pathInfo.substring(1));

            // 3. Gọi ItemDAO để tìm sản phẩm theo ID
            //    (Giả định bạn có phương thức `getItemById(int id)` trong ItemDAO)
            Item item = itemDAO.getItemById(itemId);

            if (item != null) {
                // 4. Nếu tìm thấy, chuyển đối tượng Item thành JSON và gửi về
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write(gson.toJson(item));
            } else {
                // 5. Nếu không tìm thấy sản phẩm với ID đó
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND); // Lỗi 404 Not Found
                resp.getWriter().write("{\"error\":\"Không tìm thấy sản phẩm với ID " + itemId + "\"}");
            }

        } catch (NumberFormatException e) {
            // 6. Nếu phần path không phải là một con số (ví dụ: /api/items/abc)
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); // Lỗi 400 Bad Request
            resp.getWriter().write("{\"error\":\"ID sản phẩm không hợp lệ.\"}");
        }
    }
}