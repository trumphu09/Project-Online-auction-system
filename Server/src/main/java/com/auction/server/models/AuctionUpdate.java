package com.auction.server.models;

import java.time.LocalDateTime;

public class AuctionUpdate {
    private final int auctionId;
    private final double currentHighestBid;
    private final int totalBids;
    private final LocalDateTime endTime;

    public AuctionUpdate(int auctionId, double currentHighestBid, int totalBids, LocalDateTime endTime) {
        this.auctionId = auctionId;
        this.currentHighestBid = currentHighestBid;
        this.totalBids = totalBids;
        this.endTime = endTime;
    }

    public int getAuctionId() { return auctionId; }
    public double getCurrentHighestBid() { return currentHighestBid; }
    public int getTotalBids() { return totalBids; }
    public LocalDateTime getEndTime() { return endTime; }
}