package com.auction.server.servlets;

import com.auction.server.dao.ItemDAO;
import com.auction.server.models.Item;
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

public class SearchItemsAPI extends HttpServlet {

    private final ItemDAO itemDAO = new ItemDAO();
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        Map<String, Object> responseMap = new HashMap<>();

        try {
            String keyword = req.getParameter("q");
            if (keyword == null || keyword.trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                responseMap.put("status", "error");
                responseMap.put("message", "Thiếu từ khóa tìm kiếm (tham số q).");
                resp.getWriter().write(gson.toJson(responseMap));
                return;
            }

            int page = 1;
            int limit = 10;
            if (req.getParameter("page") != null) {
                page = Integer.parseInt(req.getParameter("page"));
            }
            if (req.getParameter("limit") != null) {
                limit = Integer.parseInt(req.getParameter("limit"));
            }

            List<Item> items = itemDAO.searchItemsByName(keyword, page, limit);
            int totalItems = itemDAO.getSearchItemCount(keyword);

            responseMap.put("items", items);
            responseMap.put("totalItems", totalItems);
            responseMap.put("totalPages", (int) Math.ceil((double) totalItems / limit));
            responseMap.put("currentPage", page);

            String jsonResponse = gson.toJson(responseMap);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(jsonResponse);

        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            responseMap.put("status", "error");
            responseMap.put("message", "Tham số 'page' và 'limit' phải là số nguyên.");
            resp.getWriter().write(gson.toJson(responseMap));
        } catch (Exception e) {
            System.err.println("Lỗi không xác định trong SearchItemsAPI: " + e.getMessage());
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responseMap.put("status", "error");
            responseMap.put("message", "Đã có lỗi xảy ra ở phía máy chủ.");
            resp.getWriter().write(gson.toJson(responseMap));
        }
    }
}