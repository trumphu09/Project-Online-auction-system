package com.auction.server.models;

import java.time.LocalDateTime;

public class Payment {
    private int id;
    private int itemId;
    private int fromBidderId;
    private int toSellerId;
    private double amount;
    private PaymentStatus status;
    private LocalDateTime paymentDate;

    public Payment(int id, int itemId, int fromBidderId, int toSellerId, double amount, PaymentStatus status, LocalDateTime paymentDate) {
        this.id = id;
        this.itemId = itemId;
        this.fromBidderId = fromBidderId;
        this.toSellerId = toSellerId;
        this.amount = amount;
        this.status = status;
        this.paymentDate = paymentDate;
    }

    // Getters and Setters 
    public int getId() { return id; }
    public int getItemId() { return itemId; }
    public int getFromBidderId() { return fromBidderId; }
    public int getToSellerId() { return toSellerId; }
    public double getAmount() { return amount; }
    public PaymentStatus getStatus() { return status; }
    public LocalDateTime getPaymentDate() { return paymentDate; }
}
