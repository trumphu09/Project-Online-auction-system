package com.auction.controller;

import com.auction.service.UserService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.Map;

public class UserController {

    private final UserService userService = UserService.getInstance();
    private final Gson gson = new Gson();

    public String handleLogin(String jsonRequest) {
        try {
            JsonObject req = gson.fromJson(jsonRequest, JsonObject.class);
            if (req == null || !req.has("email") || !req.has("password")) {
                return createErrorResponse("Thiếu email hoặc mật khẩu.");
            }
            String email = req.get("email").getAsString();
            String password = req.get("password").getAsString();

            Map<String, Object> userDetails = userService.login(email, password);

            if (userDetails != null) {
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
            String role = req.has("role") ? req.get("role").getAsString() : "BIDDER";

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
}