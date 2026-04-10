package com.auction.server.models;
import java.time.LocalDateTime;

public class BidTransaction extends Entity {
    private Bidder bidder;
    private double bidAmount;
    private LocalDateTime timestamp;

    public BidTransaction(int id, Bidder bidder, double bidAmount) {
        super(id);
        this.bidder = bidder;
        this.bidAmount = bidAmount;
        this.timestamp = LocalDateTime.now();
    }

    public double getBidAmount() { return bidAmount; }
    public Bidder getBidder() { return bidder; }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

}
