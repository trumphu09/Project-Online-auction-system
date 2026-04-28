package com.auction.server.dao;

import com.auction.server.models.User;
import com.auction.server.models.UserDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.auction.server.models.Bidder;

public class UserDAO {

    public boolean registerUser(String username, String password, String email, String role) {
        String sql = "INSERT INTO users (username, password, email, role) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, email); 
            pstmt.setString(4, role);

            return pstmt.executeUpdate() > 0;
            int rowsInserted = pstmt.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi registerUser (String): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public Map<String, Object> loginUser(String email, String password){
        String sql = "SELECT id, role FROM users WHERE email = ? AND password = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)){
    public String loginUser(String email, String password){
        String sql = "SELECT role FROM users WHERE email = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
            try (java.sql.ResultSet rs = pstmt.executeQuery()){
                if (rs.next()){
                    Map<String, Object> userDetails = new HashMap<>();
                    userDetails.put("userId", rs.getInt("id"));
                    userDetails.put("role", rs.getString("role"));
                    return userDetails;
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi loginUser: " + e.getMessage());
        } catch(SQLException e){
            e.printStackTrace();
            return null;
        }
        return null;
    }
    
    public boolean updatePassword(String email, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE email = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

    public boolean updateUserRole(int userId, String newRole) {
        String sql = "UPDATE users SET role = ? WHERE id = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newRole);
            pstmt.setInt(2, userId);
            int rowsUpdated = pstmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public UserDTO getUserById(int userId) {
        String sql = "SELECT id, username, email, role FROM users WHERE id = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new UserDTO(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("role")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<UserDTO> getAllUsers() {
        List<UserDTO> users = new ArrayList<>();
        String sql = "SELECT id, username, email, role FROM users ORDER BY id";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                users.add(new UserDTO(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("email"),
                    rs.getString("role")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE id = ? AND password = ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newPassword);
            pstmt.setString(2, email);
            return pstmt.executeUpdate() > 0;
            
            pstmt.setInt(2, userId);
            pstmt.setString(3, oldPassword);
            int rowsUpdated = pstmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi updatePassword: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean registerUser(User user, String role) {
        String sql = "INSERT INTO users (username, password, email, role) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, role);
            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {

            if (pstmt.executeUpdate() > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int newIdFromDB = generatedKeys.getInt(1);
                        user.setId(newIdFromDB);
                        return true; 
                        user.setId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi registerUser (OOP): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        return false;
    }

}