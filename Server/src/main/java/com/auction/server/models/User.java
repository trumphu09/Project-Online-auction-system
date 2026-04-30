package com.auction.server.models;

public abstract class User extends Entity {

    private String username;
    private String password;
    private final String email;
    private UserRole role; // Thêm trường role

    public User(String username, String password, String email, UserRole role) {
        super();
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
    }

    public User(int id, String username, String password, String email, UserRole role) {
        super(id);
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String newUsername) {
        this.username = newUsername;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (password != null && password.length() >= 6) {
            this.password = password;
        } else {
            System.out.println("Mật khẩu không hợp lệ!");
        }
    }

    public String getEmail() {
        return email;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public abstract void showDashboard();
}