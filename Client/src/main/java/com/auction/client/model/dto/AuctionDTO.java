package com.auction.client.model.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class AuctionDTO implements Serializable {

    @Expose 
    @SerializedName("auction_id")
    private int auctionId;

    @Expose 
    @SerializedName("item_id")
    private int itemId;

    @Expose 
    @SerializedName("seller_id")
    private int sellerId;

    // Dùng String để hứng LocalDateTime từ JSON cho an toàn và dễ format lên UI
    @Expose 
    @SerializedName("end_time")
    private String endTime;

    // Dùng String để hứng Enum (OPEN, RUNNING, FINISHED...)
    @Expose 
    @SerializedName("status")
    private String status;

    @Expose 
    @SerializedName("has_extended")
    private boolean hasExtended;

    @Expose 
    @SerializedName("current_max_price")
    private double currentMaxPrice;

    @Expose 
    @SerializedName("highest_bidder_id")
    private int highestBidderId;

    // ==========================================
    // Constructor trống (Bắt buộc phải có để Gson tự động tạo Object)
    // ==========================================
    public AuctionDTO() {}

    public AuctionDTO(int auctionId, int itemId, int sellerId, String endTime, String status, 
                      boolean hasExtended, double currentMaxPrice, int highestBidderId) {
        this.auctionId = auctionId;
        this.itemId = itemId;
        this.sellerId = sellerId;
        this.endTime = endTime;
        this.status = status;
        this.hasExtended = hasExtended;
        this.currentMaxPrice = currentMaxPrice;
        this.highestBidderId = highestBidderId;
    }

    // ==========================================
    // Getters & Setters
    // ==========================================
    public int getAuctionId() { return auctionId; }
    public void setAuctionId(int auctionId) { this.auctionId = auctionId; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public int getSellerId() { return sellerId; }
    public void setSellerId(int sellerId) { this.sellerId = sellerId; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isHasExtended() { return hasExtended; }
    public void setHasExtended(boolean hasExtended) { this.hasExtended = hasExtended; }

    public double getCurrentMaxPrice() { return currentMaxPrice; }
    public void setCurrentMaxPrice(double currentMaxPrice) { this.currentMaxPrice = currentMaxPrice; }

    public int getHighestBidderId() { return highestBidderId; }
    public void setHighestBidderId(int highestBidderId) { this.highestBidderId = highestBidderId; }
}