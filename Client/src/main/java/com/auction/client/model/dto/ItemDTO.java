package com.auction.client.model.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class ItemDTO implements Serializable {
    @Expose @SerializedName("item_id") private int id;
    @Expose @SerializedName("seller_id") private int sellerId;
    @Expose @SerializedName("name") private String name;
    @Expose @SerializedName("description") private String description;
    @Expose @SerializedName("starting_price") private double startingPrice;
    @Expose @SerializedName("category") private String category;

    // === BỔ SUNG CÁC TRƯỜNG UI ĐANG THIẾU ===
    @Expose @SerializedName("price_step") private double priceStep;
    @Expose @SerializedName("start_time") private String startTime;
    @Expose @SerializedName("end_time") private String endTime;
    
    // Image path (database field)
    @Expose @SerializedName("image_path") private String imagePath;
    
    // Base64 encoded image for uploading (client → server only)
    @Expose @SerializedName("base64_image") private String base64Image;
    
    // 1. Thêm thuộc tính này để hứng dữ liệu từ DB
    @Expose @SerializedName("created_at") private String createdAt;
    @Expose @SerializedName("auction_id") private int auctionId;
    @Expose @SerializedName("current_max_price") private double currentMaxPrice;
    @Expose @SerializedName("highest_bidder_id") private int highestBidderId;
    @Expose @SerializedName("status") private String status; // Trạng thái đấu giá


    // === CONSTRUCTOR RỖNG (BẮT BUỘC PHẢI CÓ) ===
    public ItemDTO() {}

    public ItemDTO(int id, int sellerId, String name, String description, double startingPrice, String category) {
        this.id = id;
        this.sellerId = sellerId;
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
        this.category = category;
    }

    // === GETTERS & SETTERS (Bổ sung phần thiếu) ===
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getSellerId() { return sellerId; }
    public void setSellerId(int sellerId) { this.sellerId = sellerId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public double getStartingPrice() { return startingPrice; }
    public void setStartingPrice(double startingPrice) { this.startingPrice = startingPrice; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getPriceStep() { return priceStep; }
    public void setPriceStep(double priceStep) { this.priceStep = priceStep; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    public String getBase64Image() { return base64Image; }
    public void setBase64Image(String base64Image) { this.base64Image = base64Image; }
    
    public int getAuctionId() { return auctionId; }
    public void setAuctionId(int auctionId) { this.auctionId = auctionId; }
    public double getCurrentMaxPrice() { return currentMaxPrice; }
    public void setCurrentMaxPrice(double currentMaxPrice) { this.currentMaxPrice = currentMaxPrice; }
    public int getHighestBidderId() { return highestBidderId; }
    public void setHighestBidderId(int highestBidderId) { this.highestBidderId = highestBidderId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}