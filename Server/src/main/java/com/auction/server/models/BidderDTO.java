package com.auction.server.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
//DataTransferObject for Bidder, used to send bidder info to clients without exposing sensitive data like password
public class BidderDTO {
    @Expose
    @SerializedName("id")
    private int id;

    @Expose
    @SerializedName("user_name")
    private String username;

    @Expose
    @SerializedName("email")
    private String email;
    
    @Expose
    @SerializedName("account_balance")
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