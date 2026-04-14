package com.auction.server.models;
import java.time.LocalDateTime;

//DataTransferObject for BidTransaction, used to send bid transaction info to clients without exposing sensitive data
public class BidTransactionDTO {
    private int id;
    private int bidderId;
    private String bidderUsername;  // Chỉ username, không phải object
    private double bidAmount;
    private LocalDateTime timestamp;

    public BidTransactionDTO(int id, int bidderId, String bidderUsername, 
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