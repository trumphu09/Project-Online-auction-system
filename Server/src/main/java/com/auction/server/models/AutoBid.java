package com.auction.server.models;

import java.time.LocalDateTime;

public class AutoBid extends Entity {
    private final Bidder bidder;
    private final double maxBidAmount;
    private final double incrementAmount;
    private boolean isActive;
    private final LocalDateTime createdAt;

    public AutoBid(int id, Bidder bidder, double maxBidAmount, double incrementAmount) {
        super(id);
        this.bidder = bidder;
        this.maxBidAmount = maxBidAmount;
        this.incrementAmount = incrementAmount;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
    }

    public Bidder getBidder() { return bidder; }
    public double getMaxBidAmount() { return maxBidAmount; }
    public double getIncrementAmount() { return incrementAmount; }
    public boolean isActive() { return isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void deactivate() { this.isActive = false; }

    // Kiểm tra xem có thể đặt giá tự động không
    public boolean canAutoBid(double currentHighestBid) {
        return isActive && (currentHighestBid + incrementAmount) <= maxBidAmount;
    }

    // Tính giá đặt tự động tiếp theo
    public double getNextAutoBidAmount(double currentHighestBid) {
        return Math.min(currentHighestBid + incrementAmount, maxBidAmount);
    }
}