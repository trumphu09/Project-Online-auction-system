package com.auction.server.servlets;

import com.auction.server.dao.ItemDAO;
import com.auction.server.models.Item;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

public class GetMyWonItemsAPI extends HttpServlet {

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
            HttpSession session = req.getSession(false);
            Integer userId = null;
            if (session != null) {
                userId = (Integer) session.getAttribute("userId");
            }

            if (userId == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                responseMap.put("status", "error");
                responseMap.put("message", "Bạn cần đăng nhập để xem các sản phẩm đã thắng.");
                resp.getWriter().write(gson.toJson(responseMap));
                return;
            }

            List<Item> wonItems = itemDAO.getWonItemsByUserId(userId);

            String jsonResponse = gson.toJson(wonItems);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(jsonResponse);

        } catch (Exception e) {
            System.err.println("Lỗi không xác định trong GetMyWonItemsAPI: " + e.getMessage());
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responseMap.put("status", "error");
            responseMap.put("message", "Đã có lỗi xảy ra ở phía máy chủ.");
            resp.getWriter().write(gson.toJson(responseMap));
        }
    }
}