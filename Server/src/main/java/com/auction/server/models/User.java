package com.auction.server.models;

public abstract class User extends Entity {

    private String username;
    private String password;
    private final String email;
    private UserRole role;
    private boolean active = true; // Thêm lại trường active

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

    // Getters and setters
    public String getUsername() { return username; }
    public void setUsername(String newUsername) { this.username = newUsername; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getEmail() { return email; }
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    // Thêm lại các phương thức cho active
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public abstract void showDashboard();
}