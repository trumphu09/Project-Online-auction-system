package com.auction.server.servlets;

import com.auction.server.dao.BidsDAO;
import com.auction.server.models.BidTransactionDTO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.auction.server.utils.LocalDateTimeAdapter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetBidHistoryAPI extends HttpServlet {

    private final BidsDAO bidsDAO = new BidsDAO();
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        Map<String, Object> responseMap = new HashMap<>();

        try {
            String pathInfo = req.getPathInfo();
            String[] pathParts = (pathInfo != null) ? pathInfo.split("/") : new String[0];

            if (pathParts.length < 3 || !"bids".equals(pathParts[2])) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                responseMap.put("status", "error");
                responseMap.put("message", "URL không hợp lệ. Định dạng đúng: /api/items/{itemId}/bids");
                resp.getWriter().write(gson.toJson(responseMap));
                return;
            }

            int itemId = Integer.parseInt(pathParts[1]);
            if (itemId <= 0) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                responseMap.put("status", "error");
                responseMap.put("message", "ID sản phẩm không hợp lệ.");
                resp.getWriter().write(gson.toJson(responseMap));
                return;
            }

            List<BidTransactionDTO> history = bidsDAO.getBidHistoryByItemId(itemId);

            String historyJson = gson.toJson(history);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(historyJson);

        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            responseMap.put("status", "error");
            responseMap.put("message", "ID sản phẩm trong URL phải là một con số.");
            resp.getWriter().write(gson.toJson(responseMap));
        } catch (Exception e) {
            System.err.println("Lỗi không xác định trong GetBidHistoryAPI: " + e.getMessage());
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responseMap.put("status", "error");
            responseMap.put("message", "Đã có lỗi xảy ra ở phía máy chủ.");
            resp.getWriter().write(gson.toJson(responseMap));
        }
    }
}