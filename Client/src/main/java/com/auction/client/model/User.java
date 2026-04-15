package com.auction.client.model;

public class User {
    private String username;
    private String password;
    private String role;

    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // Getter và Setter
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole(){ return role; }
}