package com.auction.server.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.time.LocalDateTime;

public class PaymentDTO {
    @Expose @SerializedName("payment_id")
    private int id;
    
    @Expose @SerializedName("auction_id")
    private int auctionId;
    
    @Expose @SerializedName("bidder_id")
    private int bidderId;
    
    @Expose @SerializedName("seller_id")
    private int sellerId;
    
    @Expose @SerializedName("amount")
    private double amount;
    
    @Expose @SerializedName("status")
    private String status; // PENDING, COMPLETED, FAILED

    @Expose @SerializedName("payment_time")
    private LocalDateTime paymentTime;

    public PaymentDTO(int auctionId, int bidderId, int sellerId, double amount, String status) {
        this.auctionId = auctionId;
        this.bidderId = bidderId;
        this.sellerId = sellerId;
        this.amount = amount;
        this.status = status;
        this.paymentTime = LocalDateTime.now();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(int auctionId) {
        this.auctionId = auctionId;
    }

    public int getBidderId() {
        return bidderId;
    }

    public void setBidderId(int bidderId) {
        this.bidderId = bidderId;
    }

    public int getSellerId() {
        return sellerId;
    }

    public void setSellerId(int sellerId) {
        this.sellerId = sellerId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(LocalDateTime paymentTime) {
        this.paymentTime = paymentTime;
    }
}