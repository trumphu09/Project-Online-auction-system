package com.auction.server.servlets;

import com.auction.server.dao.BidsDAO;
import com.auction.server.models.BidTransactionDTO;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * API này xử lý yêu cầu GET để lấy lịch sử đặt giá của một sản phẩm cụ thể.
 * URL ví dụ: /api/items/1/bids
 */
public class GetBidHistoryAPI extends HttpServlet {

    private final BidsDAO bidsDAO = new BidsDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            // 1. Lấy đường dẫn đầy đủ, ví dụ: /1/bids
            String pathInfo = req.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"Thiếu ID của sản phẩm.\"}");
                return;
            }

            // 2. Tách chuỗi để lấy ra ID.
            // Ví dụ: "/1/bids" -> tách bằng "/" -> ["", "1", "bids"] -> lấy phần tử thứ 1
            String[] pathParts = pathInfo.split("/");
            if (pathParts.length < 2) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\":\"URL không hợp lệ.\"}");
                return;
            }

            int itemId = Integer.parseInt(pathParts[1]);

            // 3. Gọi đúng hàm getBidHistoryByItemId() mà bạn đã viết trong BidsDAO
            List<BidTransactionDTO> history = bidsDAO.getBidHistoryByItemId(itemId);

            // 4. Dịch danh sách DTO này thành chuỗi JSON và gửi về
            String historyJson = gson.toJson(history);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(historyJson);

        } catch (NumberFormatException e) {
            // Nếu phần ID trong URL không phải là số
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"ID sản phẩm không hợp lệ.\"}");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Lỗi phía server: " + e.getMessage() + "\"}");
        }
    }
}