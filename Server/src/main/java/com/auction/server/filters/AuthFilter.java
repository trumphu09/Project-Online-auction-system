package com.auction.server.filters;

import com.auction.server.models.UserRole;
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

        if (path.equals("/api/login") ||
            path.equals("/api/register") ||

            // public GET items
            (path.equals("/api/items") && req.getMethod().equals("GET")) ||
            path.startsWith("/api/items/") ||

            // public search/category
            path.startsWith("/api/search/") ||
            path.startsWith("/api/categories/") ||

            // PUBLIC IMAGE API
            path.startsWith("/api/images/"))
        {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Bạn cần đăng nhập để thực hiện chức năng này.");
            return;
        }

        UserRole userRole = (UserRole) session.getAttribute("userRole");

        if (path.startsWith("/api/admin/")) {
            if (userRole != UserRole.ADMIN) {
                sendError(resp, HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền thực hiện hành động này.");
                return;
            }
        }
        
        if (path.equals("/api/items") && req.getMethod().equals("POST")) {
            if (userRole != UserRole.SELLER && userRole != UserRole.ADMIN) {
                sendError(resp, HttpServletResponse.SC_FORBIDDEN, "Chỉ người bán (SELLER) mới có thể đăng bán sản phẩm.");
                return;
            }
        }

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
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}
}