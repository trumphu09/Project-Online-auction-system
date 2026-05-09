package com.auction.client.model.dto;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class UserDTO implements Serializable {
    private int id;
    private String username;
    private String email;
    
    @SerializedName("full_name")
    private String fullName;
    
    private String role; // "ADMIN", "BIDDER", "SELLER"
    
    @SerializedName("balance")
    private double balance;

    // --- BỔ SUNG CÁC TRƯỜNG THỐNG KÊ CỦA SELLER ---
    @SerializedName("total_rating")
    private double totalRating;

    @SerializedName("sale_count")
    private int saleCount;
    // Thêm trường này:
    @SerializedName("isActive")
    private boolean active;

    // Thêm getter/setter:
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    // --- Getters and Setters ---
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

    public double getTotalRating() { return totalRating; }
    public void setTotalRating(double totalRating) { this.totalRating = totalRating; }

    public int getSaleCount() { return saleCount; }
    public void setSaleCount(int saleCount) { this.saleCount = saleCount; }
}