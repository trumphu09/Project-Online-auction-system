package com.auction.server.models;

public abstract class User extends Entity {

    private String username;
    private String password;
    private final String email;
    private UserRole role;
    private AccountStatus status; // Thay thế boolean active

    public User(String username, String password, String email, UserRole role) {
        super();
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.status = AccountStatus.ACTIVE; // Mặc định là ACTIVE
    }

    public User(int id, String username, String password, String email, UserRole role, AccountStatus status) {
        super(id);
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.status = status;
    }

    // Getters and setters
    public String getUsername() { return username; }
    public void setUsername(String newUsername) { this.username = newUsername; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getEmail() { return email; }
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }

    public abstract void showDashboard();
}