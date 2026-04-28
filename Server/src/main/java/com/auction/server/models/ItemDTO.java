package com.auction.server.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Lớp DTO cơ sở cho Hàng hóa.
 * Chỉ chứa các thông tin tĩnh của món hàng, KHÔNG chứa trạng thái phiên đấu giá.
 */
public class ItemDTO {

    @Expose
    @SerializedName("item_id")
    private int id;

    @Expose
    @SerializedName("seller_id")
    private int sellerId;

    @Expose
    @SerializedName("name")
    private String name;

    @Expose
    @SerializedName("description")
    private String description;

    @Expose
    @SerializedName("starting_price")
    private double startingPrice;

    @Expose
    @SerializedName("category")
    private String category;

    // ==========================================
    // Constructor
    // ==========================================
    public ItemDTO(int id, int sellerId, String name, String description, double startingPrice, String category) {
        this.id = id;
        this.sellerId = sellerId;
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
        this.category = category;
    }

    // ==========================================
    // Getters & Setters
    // ==========================================
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
}