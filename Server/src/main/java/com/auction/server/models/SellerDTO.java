package com.auction.server.models;

//DataTransferObject for Seller, used to send seller info to clients without exposing sensitive data like password
public class SellerDTO {
    private int id;
    private String username;
    private String email;
    private double accountBalance;
    private double totalRating;
    private int saleCount;

    public SellerDTO(int id, String username, String email, double accountBalance, 
                     double totalRating, int saleCount) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.accountBalance = accountBalance;
        this.totalRating = totalRating;
        this.saleCount = saleCount;
    }

    // Getters only
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public double getAccountBalance() { return accountBalance; }
    public double getTotalRating() { return totalRating; }
    public int getSaleCount() { return saleCount; }
}