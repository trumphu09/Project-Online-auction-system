package com.auction.server.servlets;

import com.auction.server.dao.ItemDAO;
import com.auction.server.models.Item;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.auction.server.utils.LocalDateTimeAdapter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemsAPI extends HttpServlet {

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
            int page = 1;
            int limit = 10;
            if (req.getParameter("page") != null) {
                page = Integer.parseInt(req.getParameter("page"));
            }
            if (req.getParameter("limit") != null) {
                limit = Integer.parseInt(req.getParameter("limit"));
            }

            List<Item> items = itemDAO.getAllItems(page, limit);
            int totalItems = itemDAO.getTotalItemCount();

            responseMap.put("items", items);
            responseMap.put("totalItems", totalItems);
            responseMap.put("totalPages", (int) Math.ceil((double) totalItems / limit));
            responseMap.put("currentPage", page);

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(gson.toJson(responseMap));

        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            responseMap.put("status", "error");
            responseMap.put("message", "Tham số 'page' và 'limit' phải là số nguyên.");
            resp.getWriter().write(gson.toJson(responseMap));
        } catch (Exception e) {
            System.err.println("Lỗi không xác định trong ItemsAPI (GET): " + e.getMessage());
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responseMap.put("status", "error");
            responseMap.put("message", "Đã có lỗi xảy ra ở phía máy chủ.");
            resp.getWriter().write(gson.toJson(responseMap));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        Map<String, Object> responseMap = new HashMap<>();

        try {
            HttpSession session = req.getSession(false);
            // Filter đã đảm bảo session và role hợp lệ
            int sellerId = (Integer) session.getAttribute("userId");

            Item newItem;
            try {
                String requestBody = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
                newItem = gson.fromJson(requestBody, Item.class);
                if (newItem == null) {
                    throw new JsonSyntaxException("Request body is empty or malformed.");
                }
            } catch (JsonSyntaxException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                responseMap.put("status", "error");
                responseMap.put("message", "Dữ liệu JSON không hợp lệ.");
                resp.getWriter().write(gson.toJson(responseMap));
                return;
            }
            
            newItem.setSellerId(sellerId);

            boolean isSuccess = itemDAO.addItem(newItem);

            if (isSuccess) {
                responseMap.put("status", "success");
                responseMap.put("message", "Đăng bán sản phẩm thành công!");
                responseMap.put("itemId", newItem.getId());
                resp.setStatus(HttpServletResponse.SC_CREATED);
            } else {
                responseMap.put("status", "error");
                responseMap.put("message", "Không thể lưu sản phẩm vào cơ sở dữ liệu.");
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            System.err.println("Lỗi không xác định trong ItemsAPI (POST): " + e.getMessage());
            e.printStackTrace();
            responseMap.put("status", "error");
            responseMap.put("message", "Đã có lỗi xảy ra ở phía máy chủ.");
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            resp.getWriter().write(gson.toJson(responseMap));
        }
    }
}