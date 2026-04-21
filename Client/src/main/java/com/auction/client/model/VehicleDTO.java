package com.auction.client.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VehicleDTO extends ItemDTO {
<<<<<<< HEAD
    @Expose @SerializedName("brand")
    private String brand;

    @Expose @SerializedName("mileage")
    private int mileage;

    @Expose @SerializedName("condition")
    private String condition;

    public VehicleDTO(int id, int sellerId, String name, String description, double startingPrice, 
                      String brand, int mileage, String condition) {
        super(id, sellerId, name, description, startingPrice, "VEHICLE");
        this.brand = brand;
        this.mileage = mileage;
        this.condition = condition;
    }

    // Getters & Setters
=======
    // Thêm các trường riêng của bảng vehicles
    private String brand;
    private int mileage;
    private String condition;

    public VehicleDTO() { super(); }

    public VehicleDTO(String name, double startingPrice, double priceStep, String description, String imagePath, int sellerId, String startTime, String endTime) {
        super(name, startingPrice, priceStep, description, imagePath, sellerId, startTime, endTime);
    }

    @Override
    public String getCategory() { return "Vehicle"; }

>>>>>>> 71e6ab4a2b9c335e64205860a7a9ead1080a473c
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public int getMileage() { return mileage; }
    public void setMileage(int mileage) { this.mileage = mileage; }
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
}