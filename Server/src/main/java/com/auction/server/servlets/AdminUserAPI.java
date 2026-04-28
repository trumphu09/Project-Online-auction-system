package com.auction.server.servlets;

import com.auction.server.dao.UserDAO;
import com.auction.server.models.UserDTO;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminUserAPI extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        Map<String, Object> responseMap = new HashMap<>();

        try {
            // Filter đã đảm bảo quyền Admin
            List<UserDTO> users = userDAO.getAllUsers();
            String jsonResponse = gson.toJson(users);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(jsonResponse);

        } catch (Exception e) {
            System.err.println("Lỗi không xác định trong AdminUserAPI (GET): " + e.getMessage());
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responseMap.put("status", "error");
            responseMap.put("message", "Đã có lỗi xảy ra ở phía máy chủ.");
            resp.getWriter().write(gson.toJson(responseMap));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        Map<String, Object> responseMap = new HashMap<>();

        try {
            // Filter đã đảm bảo quyền Admin
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                responseMap.put("status", "error");
                responseMap.put("message", "Thiếu ID của người dùng cần cập nhật.");
                resp.getWriter().write(gson.toJson(responseMap));
                return;
            }
            int userIdToUpdate = Integer.parseInt(pathInfo.substring(1));
            if (userIdToUpdate <= 0) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                responseMap.put("status", "error");
                responseMap.put("message", "ID người dùng không hợp lệ.");
                resp.getWriter().write(gson.toJson(responseMap));
                return;
            }

            String newRole;
            try {
                String requestBody = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
                JsonObject requestJson = gson.fromJson(requestBody, JsonObject.class);

                if (requestJson == null || !requestJson.has("role")) {
                    throw new JsonSyntaxException("Missing 'role' field in JSON body.");
                }
                newRole = requestJson.get("role").getAsString();

                List<String> validRoles = Arrays.asList("BIDDER", "SELLER", "INACTIVE");
                if (newRole == null || newRole.trim().isEmpty() || !validRoles.contains(newRole)) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    responseMap.put("status", "error");
                    responseMap.put("message", "Trạng thái mới không hợp lệ. Chỉ chấp nhận 'BIDDER', 'SELLER', hoặc 'INACTIVE'.");
                    resp.getWriter().write(gson.toJson(responseMap));
                    return;
                }
            } catch (JsonSyntaxException | NullPointerException ex) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                responseMap.put("status", "error");
                responseMap.put("message", "Dữ liệu JSON không hợp lệ hoặc thiếu trường 'role'.");
                resp.getWriter().write(gson.toJson(responseMap));
                return;
            }

            boolean isSuccess = userDAO.updateUserRole(userIdToUpdate, newRole);

            if (isSuccess) {
                responseMap.put("status", "success");
                responseMap.put("message", "Cập nhật trạng thái người dùng thành công.");
                resp.setStatus(HttpServletResponse.SC_OK);
            } else {
                responseMap.put("status", "error");
                responseMap.put("message", "Không tìm thấy người dùng với ID được cung cấp.");
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            responseMap.put("status", "error");
            responseMap.put("message", "ID người dùng trong URL phải là một con số.");
        } catch (Exception e) {
            System.err.println("Lỗi không xác định trong AdminUserAPI (PUT): " + e.getMessage());
            e.printStackTrace();
            responseMap.put("status", "error");
            responseMap.put("message", "Đã có lỗi xảy ra ở phía máy chủ.");
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            resp.getWriter().write(gson.toJson(responseMap));
        }
    }
}