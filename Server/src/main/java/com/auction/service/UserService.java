package com.auction.service;

import com.auction.server.dao.UserDAO;
import java.util.Map;

public class UserService {

    private static final UserService instance = new UserService();
    private final UserDAO userDAO;

    private UserService() {
        this.userDAO = new UserDAO();
    }

    public static UserService getInstance() {
        return instance;
    }

    public Map<String, Object> login(String email, String password) {
        if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
            return null;
        }
        return userDAO.loginUser(email, password);
    }

    public boolean register(String username, String password, String email, String role) {
        if (username == null || username.trim().isEmpty() || 
            password == null || password.isEmpty() || 
            email == null || email.trim().isEmpty()) {
            return false;
        }
        // Mặc định vai trò là BIDDER nếu không được chỉ định
        String userRole = (role != null && !role.trim().isEmpty()) ? role : "BIDDER";
        return userDAO.registerUser(username, password, email, userRole);
    }
}