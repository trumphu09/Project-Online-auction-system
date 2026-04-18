package com.auction.client.model;

public class VehicleDTO extends ItemDTO {
    // Thêm các trường riêng của bảng vehicles
    private String brand;
    private int mileage;
    private String condition;

    public VehicleDTO() { super(); }

    @Override
    public String getCategory() { return "Vehicle"; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public int getMileage() { return mileage; }
    public void setMileage(int mileage) { this.mileage = mileage; }
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
}