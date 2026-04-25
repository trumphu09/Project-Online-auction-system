package com.auction.server.servlets;

import com.auction.server.dao.ItemDAO;
import com.google.gson.Gson;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * API dành cho Admin để quản lý sản phẩm.
 * - DELETE /api/admin/items/{itemId}: Xóa một sản phẩm.
 */
public class AdminItemAPI extends HttpServlet {

    private final ItemDAO itemDAO = new ItemDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        Map<String, Object> responseMap = new HashMap<>();

        try {
            HttpSession session = req.getSession(false);
            if (session == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                responseMap.put("status", "error");
                responseMap.put("message", "Bạn cần đăng nhập để thực hiện hành động này.");
                resp.getWriter().write(gson.toJson(responseMap));
                return;
            }
            if (!"ADMIN".equals(session.getAttribute("userRole"))) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                responseMap.put("status", "error");
                responseMap.put("message", "Bạn không có quyền thực hiện hành động này.");
                resp.getWriter().write(gson.toJson(responseMap));
                return;
            }

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
                resp.setStatus(HttpServletResponse.SC_OK);
                responseMap.put("status", "success");
                responseMap.put("message", "Xóa sản phẩm thành công.");
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                responseMap.put("status", "error");
                responseMap.put("message", "Không thể xóa sản phẩm. Sản phẩm có thể không tồn tại.");
            }

        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            responseMap.put("status", "error");
            responseMap.put("message", "ID sản phẩm trong URL phải là một con số.");
        } catch (Exception e) {
            System.err.println("Lỗi không xác định trong AdminItemAPI: " + e.getMessage());
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responseMap.put("status", "error");
            responseMap.put("message", "Đã có lỗi xảy ra ở phía máy chủ.");
        } finally {
            resp.getWriter().write(gson.toJson(responseMap));
        }
    }
}