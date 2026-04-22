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

/**
 * API này xử lý yêu cầu GET để lấy danh sách các sản phẩm mà một user đã thắng.
 * URL ví dụ: /api/users/2/won-items
 */
public class GetMyWonItemsAPI extends HttpServlet {

    private final ItemDAO itemDAO = new ItemDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            // Lấy đường dẫn, ví dụ: /2/won-items
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"Thiếu ID của người dùng.\"}");
                return;
            }

            // Tách chuỗi để lấy ra userId
            String[] pathParts = pathInfo.split("/");
            if (pathParts.length < 2) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"URL không hợp lệ.\"}");
                return;
            }

            int userId = Integer.parseInt(pathParts[1]);

            // Gọi phương thức mới trong ItemDAO
            List<Item> wonItems = itemDAO.getWonItemsByUserId(userId);

            // Dịch kết quả thành JSON và gửi về
            String jsonResponse = gson.toJson(wonItems);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(jsonResponse);

        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"ID người dùng không hợp lệ.\"}");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Lỗi phía server: " + e.getMessage() + "\"}");
        }
    }
}