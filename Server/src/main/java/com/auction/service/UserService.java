package com.auction.service;

import com.auction.server.dao.UserDAO;
import com.auction.server.models.UserDTO;
import java.util.List;
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
        String userRole = (role != null && !role.trim().isEmpty()) ? role : "BIDDER";
        return userDAO.registerUser(username, password, email, userRole);
    }

    public UserDTO getProfile(int userId) {
        if (userId <= 0) {
            return null;
        }
        return userDAO.getUserById(userId);
    }

    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        if (userId <= 0 || oldPassword == null || oldPassword.isEmpty() || newPassword == null || newPassword.isEmpty()) {
            return false;
        }
        return userDAO.changePassword(userId, oldPassword, newPassword);
    }

    public List<UserDTO> getAllUsers() {
        return userDAO.getAllUsers();
    }

    public boolean updateUserRole(int userId, String newRole) {
        if (userId <= 0 || newRole == null || newRole.trim().isEmpty()) {
            return false;
        }
        return userDAO.updateUserRole(userId, newRole);
    }
}