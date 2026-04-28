package com.auction.server.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.time.LocalDateTime;

/**
 * Lớp DTO (Data Transfer Object) dùng để chứa dữ liệu thô của bảng auctions.
 * Nó siêu nhẹ vì chỉ chứa ID của Item và Seller thay vì chứa cả 1 Object to đùng.
 */
public class AuctionDataDTO {

    @Expose
    @SerializedName("auction_id")
    private int auctionId;

    @Expose
    @SerializedName("item_id")
    private int itemId;

    @Expose
    @SerializedName("seller_id")
    private int sellerId;

    @Expose
    @SerializedName("end_time")
    private LocalDateTime endTime;

    @Expose
    private AuctionStatus status;

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
    // Constructor (Phải khớp với lời gọi trong AuctionDAO)
    // ==========================================
    public AuctionDataDTO(int auctionId, int itemId, int sellerId, LocalDateTime endTime, AuctionStatus status, boolean hasExtended) {
        this.auctionId = auctionId;
        this.itemId = itemId;
        this.sellerId = sellerId;
        this.endTime = endTime;
        this.status = status;
        this.hasExtended = hasExtended;
        
        // Mặc định khi mới tạo object, có thể chưa có ai đặt giá
        this.currentMaxPrice = 0.0;
        this.highestBidderId = 0;
    }

    // ==========================================
    // Getters & Setters
    // ==========================================
    
    public int getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(int auctionId) {
        this.auctionId = auctionId;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getSellerId() {
        return sellerId;
    }

    public void setSellerId(int sellerId) {
        this.sellerId = sellerId;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public AuctionStatus getStatus() {
        return status;
    }

    public void setStatus(AuctionStatus status) {
        this.status = status;
    }

    public boolean isHasExtended() {
        return hasExtended;
    }

    public void setHasExtended(boolean hasExtended) {
        this.hasExtended = hasExtended;
    }

    // Bổ sung Getter/Setter chuẩn cho giá và người mua
    public int getHighestBidderId() {
        return highestBidderId;
    }

    public void setHighestBidderId(int highestBidderId) {
        this.highestBidderId = highestBidderId;
    }

    public double getCurrentMaxPrice() {
        return currentMaxPrice;
    }

    public void setCurrentMaxPrice(double currentMaxPrice) {
        this.currentMaxPrice = currentMaxPrice;
    }
}