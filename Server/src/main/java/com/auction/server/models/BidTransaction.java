package com.auction.server.models;
import java.time.LocalDateTime;

public class BidTransaction {
    private final Bidder bidder;
    private final double bidAmount;
    private final LocalDateTime timestamp;

    public BidTransaction(Bidder bidder, double bidAmount) {
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
