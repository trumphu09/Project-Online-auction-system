package com.auction.controller;

import com.auction.service.UserService;
import com.auction.server.dao.AdminDAO;
import com.auction.server.models.UserDTO;
import com.auction.server.models.UserRole;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.List;
import java.util.Map;
import com.auction.server.dao.AdminDAO;

public class UserController {

    private final UserService userService = UserService.getInstance();
    private final Gson gson = new Gson();

    public String handleLogin(String jsonRequest) {
        try {
            JsonObject req = gson.fromJson(jsonRequest, JsonObject.class);
            if (req == null || !req.has("email") || !req.has("password")) {
                return createErrorResponse("Thiếu email hoặc mật khẩu.");
            }
            String email    = req.get("email").getAsString();
            String password = req.get("password").getAsString();

            Map<String, Object> userDetails = userService.login(email, password);

            if (userDetails != null) {
                // ✅ THÊM: Kiểm tra bị banned trước khi cho vào
                if (userDetails.containsKey("banned")) {
                    return createErrorResponse("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ Admin.");
                }
                return createSuccessResponse("Đăng nhập thành công!", gson.toJsonTree(userDetails));
            } else {
                return createErrorResponse("Email hoặc mật khẩu không đúng.");
            }
        } catch (JsonSyntaxException e) {
            return createErrorResponse("Dữ liệu JSON không hợp lệ.");
        }
    }

    public String handleRegister(String jsonRequest) {
        try {
            JsonObject req = gson.fromJson(jsonRequest, JsonObject.class);
            if (req == null || !req.has("username") || !req.has("password") || !req.has("email")) {
                return createErrorResponse("Thiếu thông tin bắt buộc.");
            }
            String username = req.get("username").getAsString();
            String password = req.get("password").getAsString();
            String email = req.get("email").getAsString();
            UserRole role = req.has("role") ? UserRole.fromString(req.get("role").getAsString()) : UserRole.BIDDER;

            boolean isSuccess = userService.register(username, password, email, role);

            if (isSuccess) {
                return createSuccessResponse("Đăng ký thành công!", null);
            } else {
                return createErrorResponse("Đăng ký thất bại. Email hoặc username có thể đã tồn tại.");
            }
        } catch (JsonSyntaxException e) {
            return createErrorResponse("Dữ liệu JSON không hợp lệ.");
        }
    }

    public String handleGetProfile(int userId) {
        try {
            UserDTO profile = userService.getProfile(userId);
            if (profile != null) {
                return createSuccessResponse("Lấy thông tin hồ sơ thành công.", gson.toJsonTree(profile));
            } else {
                return createErrorResponse("Không tìm thấy người dùng.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("Lỗi hệ thống.");
        }
    }

    public String handleChangePassword(int userId, String jsonRequest) {
        try {
            JsonObject req = gson.fromJson(jsonRequest, JsonObject.class);
            if (req == null || !req.has("oldPassword") || !req.has("newPassword")) {
                return createErrorResponse("Thiếu mật khẩu cũ hoặc mật khẩu mới.");
            }
            String oldPassword = req.get("oldPassword").getAsString();
            String newPassword = req.get("newPassword").getAsString();

            boolean isSuccess = userService.changePassword(userId, oldPassword, newPassword);

            if (isSuccess) {
                return createSuccessResponse("Đổi mật khẩu thành công.", null);
            } else {
                return createErrorResponse("Đổi mật khẩu thất bại. Mật khẩu cũ không đúng.");
            }
        } catch (JsonSyntaxException e) {
            return createErrorResponse("Dữ liệu JSON không hợp lệ.");
        }
    }

    public String handleGetAllUsers() {
        try {
            List<UserDTO> users = userService.getAllUsers();
            return createSuccessResponse("Lấy danh sách người dùng thành công.", gson.toJsonTree(users));
        } catch (Exception e) {
            e.printStackTrace();
            return createErrorResponse("Lỗi hệ thống.");
        }
    }

    public String handleUpdateUserRole(int userId, String jsonRequest) {
        try {
            JsonObject req = gson.fromJson(jsonRequest, JsonObject.class);
            if (req == null || !req.has("role")) {
                return createErrorResponse("Thiếu vai trò mới.");
            }
            UserRole newRole = UserRole.fromString(req.get("role").getAsString());

            boolean isSuccess = userService.updateUserRole(userId, newRole);

            if (isSuccess) {
                return createSuccessResponse("Cập nhật vai trò thành công.", null);
            } else {
                return createErrorResponse("Cập nhật vai trò thất bại.");
            }
        } catch (JsonSyntaxException e) {
            return createErrorResponse("Dữ liệu JSON không hợp lệ.");
        }
    }

    private String createSuccessResponse(String message, com.google.gson.JsonElement data) {
        JsonObject response = new JsonObject();
        response.addProperty("status", "success");
        response.addProperty("message", message);
        if (data != null) {
            response.add("data", data);
        }
        return gson.toJson(response);
    }

    private String createErrorResponse(String message) {
        JsonObject response = new JsonObject();
        response.addProperty("status", "error");
        response.addProperty("message", message);
        return gson.toJson(response);
    }

    // UserController.java — thêm method này
    public String handleBanUser(int userId, String jsonRequest) {
        try {
            JsonObject req = gson.fromJson(jsonRequest, JsonObject.class);
            if (req == null || !req.has("action")) {
                return createErrorResponse("Thiếu trường 'action' (ban/unban).");
            }

            String action = req.get("action").getAsString();
            boolean shouldActive;

            if ("ban".equalsIgnoreCase(action)) {
                shouldActive = false;   // Khóa
            } else if ("unban".equalsIgnoreCase(action)) {
                shouldActive = true;    // Mở khóa
            } else {
                return createErrorResponse("Hành động không hợp lệ. Chỉ chấp nhận 'ban' hoặc 'unban'.");
            }

            // AdminDAO đã có sẵn hàm setUserStatus
            AdminDAO adminDAO = new AdminDAO();
            boolean success = adminDAO.setUserStatus(userId, shouldActive);

            if (success) {
                String msg = shouldActive ? "Đã mở khóa tài khoản thành công." : "Đã khóa tài khoản thành công.";
                return createSuccessResponse(msg, null);
            } else {
                return createErrorResponse("Không tìm thấy người dùng hoặc lỗi hệ thống.");
            }

        } catch (JsonSyntaxException e) {
            return createErrorResponse("Dữ liệu JSON không hợp lệ.");
        }
    }
}