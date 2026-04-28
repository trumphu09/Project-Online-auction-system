package com.auction.server.servlets;

import com.auction.server.dao.ItemDAO;
import com.google.gson.Gson;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AdminItemAPI extends HttpServlet {

    private final ItemDAO itemDAO = new ItemDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        Map<String, Object> responseMap = new HashMap<>();

        try {
            // Filter đã đảm bảo quyền Admin
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                responseMap.put("status", "error");
                responseMap.put("message", "Thiếu ID của sản phẩm cần xóa.");
                resp.getWriter().write(gson.toJson(responseMap));
                return;
            }
            int itemIdToDelete = Integer.parseInt(pathInfo.substring(1));
            if (itemIdToDelete <= 0) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                responseMap.put("status", "error");
                responseMap.put("message", "ID sản phẩm không hợp lệ.");
                resp.getWriter().write(gson.toJson(responseMap));
                return;
            }

            boolean isSuccess = itemDAO.deleteItem(itemIdToDelete);

            if (isSuccess) {
                responseMap.put("status", "success");
                responseMap.put("message", "Xóa sản phẩm thành công.");
                resp.setStatus(HttpServletResponse.SC_OK);
            } else {
                responseMap.put("status", "error");
                responseMap.put("message", "Không thể xóa sản phẩm. Sản phẩm có thể không tồn tại.");
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            responseMap.put("status", "error");
            responseMap.put("message", "ID sản phẩm trong URL phải là một con số.");
        } catch (Exception e) {
            System.err.println("Lỗi không xác định trong AdminItemAPI: " + e.getMessage());
            e.printStackTrace();
            responseMap.put("status", "error");
            responseMap.put("message", "Đã có lỗi xảy ra ở phía máy chủ.");
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            resp.getWriter().write(gson.toJson(responseMap));
        }
    }
}