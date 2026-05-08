package com.auction.client.model.dto;

import java.io.Serializable;

public class UserDTO implements Serializable {
    private int id;
    private String username;
    private String email;
    private String fullName;
    private String role; // "ADMIN", "BIDDER", "SELLER"
    private double balance;

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
}