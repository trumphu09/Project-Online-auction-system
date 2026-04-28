package com.auction.server.filters;

import com.google.gson.Gson;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AuthFilter implements Filter {

    private final Gson gson = new Gson();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String path = req.getRequestURI();

        // Cho phép các API công khai đi qua mà không cần kiểm tra
        if (path.equals("/api/login") || path.equals("/api/register") || 
            (path.equals("/api/items") && req.getMethod().equals("GET")) ||
            path.startsWith("/api/items/") || path.startsWith("/api/search/") ||
            path.startsWith("/api/categories/")) {
            chain.doFilter(request, response);
            return;
        }

        // Từ đây trở đi, tất cả các API đều yêu cầu đăng nhập
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Bạn cần đăng nhập để thực hiện chức năng này.");
            return;
        }

        // Kiểm tra quyền ADMIN cho các API admin
        if (path.startsWith("/api/admin/")) {
            String userRole = (String) session.getAttribute("userRole");
            if (!"ADMIN".equals(userRole)) {
                sendError(resp, HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền thực hiện hành động này.");
                return;
            }
        }
        
        // Kiểm tra quyền SELLER cho API đăng bán
        if (path.equals("/api/items") && req.getMethod().equals("POST")) {
            String userRole = (String) session.getAttribute("userRole");
            if (!"SELLER".equals(userRole) && !"ADMIN".equals(userRole)) {
                sendError(resp, HttpServletResponse.SC_FORBIDDEN, "Chỉ người bán (SELLER) mới có thể đăng bán sản phẩm.");
                return;
            }
        }

        // Nếu tất cả kiểm tra đều qua, cho phép yêu cầu đi tiếp
        chain.doFilter(request, response);
    }

    private void sendError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("status", "error");
        responseMap.put("message", message);
        resp.getWriter().write(gson.toJson(responseMap));
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Không cần implement
    }

    @Override
    public void destroy() {
        // Không cần implement
    }
}