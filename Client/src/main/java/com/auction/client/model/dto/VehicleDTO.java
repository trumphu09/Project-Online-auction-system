package com.auction.client.model.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VehicleDTO extends ItemDTO {
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
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public int getMileage() { return mileage; }
    public void setMileage(int mileage) { this.mileage = mileage; }
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
}