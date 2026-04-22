package com.auction.server.servlets;
//lấy thông tin tất cả các sản phẩm
import com.auction.server.dao.ItemDAO;
import com.auction.server.models.Item;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class GetItemsAPI extends HttpServlet {

    private final ItemDAO itemDAO = new ItemDAO();
    private final Gson gson = new Gson();

    /**
     * Xử lý yêu cầu GET để lấy danh sách tất cả các sản phẩm.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 1. Gọi ItemDAO để lấy tất cả sản phẩm từ database.
        //    (Giả định rằng bạn có một phương thức như `getAllItems()` trong ItemDAO)
        List<Item> items = itemDAO.getAllItems();

        // 2. Dùng Gson để chuyển đổi danh sách đối tượng Item thành một chuỗi JSON.
        String itemsJson = gson.toJson(items);

        // 3. Thiết lập header cho response để báo cho client biết đây là dữ liệu JSON.
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        // 4. Ghi chuỗi JSON vào body của response và gửi về cho client.
        resp.getWriter().write(itemsJson);
    }
}