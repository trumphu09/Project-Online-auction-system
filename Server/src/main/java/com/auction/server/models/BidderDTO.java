package com.auction.server.models;
//DataTransferObject for Bidder, used to send bidder info to clients without exposing sensitive data like password
public class BidderDTO {
    private int id;
    private String username;
    private String email;
    private double accountBalance;

    public BidderDTO(int id, String username, String email, double accountBalance) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.accountBalance = accountBalance;
    }

    // Getters only (no password!)
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public double getAccountBalance() { return accountBalance; }
}