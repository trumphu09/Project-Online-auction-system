package com.auction.client.model;
<<<<<<< HEAD

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public abstract class ItemDTO implements Serializable {
    @Expose @SerializedName("item_id")
    private int id;

    @Expose @SerializedName("seller_id")
    private int sellerId;

    @Expose @SerializedName("name")
    private String name;

    @Expose @SerializedName("description")
    private String description;

    @Expose @SerializedName("starting_price")
    private double startingPrice;

    @Expose @SerializedName("category")
    private String category;

    public ItemDTO(int id, int sellerId, String name, String description, double startingPrice, String category) {
        this.id = id;
=======
import java.io.Serializable;

public abstract class ItemDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    // --- CÁC TRƯỜNG CHUẨN KHỚP VỚI DATABASE ---
    private int id; 
    private int sellerId;
    private String name;
    private String description;
    private double startingPrice;
    private double priceStep;
    private String imagePath;
    private double currentMaxPrice; // Bổ sung
    private int highestBidderId;    // Bổ sung
    private String status;          // Bổ sung ("OPEN", "RUNNING", "FINISHED"...)
    private String startTime;       
    private String endTime;

    public ItemDTO() {}

    public ItemDTO(String name, double startingPrice, double priceStep, String description, String imagePath, int sellerId, String startTime, String endTime) {
        this.name = name;
        this.startingPrice = startingPrice;
        this.priceStep = priceStep;
        this.description = description;
        this.imagePath = imagePath;
>>>>>>> 71e6ab4a2b9c335e64205860a7a9ead1080a473c
        this.sellerId = sellerId;
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
        this.category = category;
    }

<<<<<<< HEAD
    // Getters & Setters
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
=======
    // Bổ sung các Getters và Setters cho các trường mới (id, currentMaxPrice, highestBidderId, status)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public double getCurrentMaxPrice() { return currentMaxPrice; }
    public void setCurrentMaxPrice(double currentMaxPrice) { this.currentMaxPrice = currentMaxPrice; }

    public int getHighestBidderId() { return highestBidderId; }
    public void setHighestBidderId(int highestBidderId) { this.highestBidderId = highestBidderId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getStartingPrice() { return startingPrice; }
    public void setStartingPrice(double startingPrice) { this.startingPrice = startingPrice; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getSellerId() { return sellerId; }
    public void setSellerId(int sellerId) { this.sellerId = sellerId; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    
    public double getPriceStep() { return priceStep; }
    public void setPriceStep(double priceStep) { this.priceStep = priceStep; }
    
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public abstract String getCategory();
>>>>>>> 71e6ab4a2b9c335e64205860a7a9ead1080a473c
}