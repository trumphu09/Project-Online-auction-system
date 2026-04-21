package com.auction.client.model;

import java.time.LocalDateTime;

public class BidDTO {
    private int id;
    private int bidderId;
    private String bidderUsername;  // Chỉ username, không phải object
    private double bidAmount;
    private LocalDateTime timestamp;

    public BidDTO(int id, int bidderId, String bidderUsername, 
                            double bidAmount, LocalDateTime timestamp) {
        this.id = id;
        this.bidderId = bidderId;
        this.bidderUsername = bidderUsername;
        this.bidAmount = bidAmount;
        this.timestamp = timestamp;
    }

    // Getters only
    public int getId() { return id; }
    public int getBidderId() { return bidderId; }
    public String getBidderUsername() { return bidderUsername; }
    public double getBidAmount() { return bidAmount; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
