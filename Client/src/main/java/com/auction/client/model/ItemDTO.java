package com.auction.client.model;
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
        this.sellerId = sellerId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

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
}