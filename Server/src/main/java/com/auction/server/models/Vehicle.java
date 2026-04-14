package com.auction.server.models;

import java.time.LocalDateTime;

public class Vehicle extends Item {
    private  String brand;
    private  int mileage;
    private  String condition;

    public Vehicle(int id,int sellerId, String name, String description, double startingPrice, LocalDateTime startTime, LocalDateTime endTime,
                   String brand, int mileage, String condition) {
        super(id, sellerId, name, description, startingPrice, startTime, endTime, "VEHICLE");
        this.brand = brand;
        this.mileage = mileage;
        this.condition = condition;
    }

    // constructor co day du tat ca thuoc tinh 
    public Vehicle(int id,int sellerId, String name, String description, double startingPrice, double currentPrice, LocalDateTime startTime, LocalDateTime endTime, AuctionStatus status, int highestBidderId,
                   String brand, int mileage, String condition) {
        super(id, sellerId, name, description, startingPrice, currentPrice, startTime, endTime, status, highestBidderId,"VEHICLE");
        this.brand = brand;
        this.mileage = mileage;
        this.condition = condition;
    }
    @Override
    public void printInfo() {
        System.out.println("--- Phương tiện di chuyển ---");
        System.out.println("Xe: " + brand + " " + getName());
        System.out.println("Tình trạng: " + condition + " | Số km đã đi: " + mileage + " km");
        System.out.println("Giá khởi điểm: $" + getStartingPrice());
    }

    // getters
    public String getBrand() {
        return brand;
    }   
    public int getMileage() {
        return mileage;
    }
    public String getCondition() {
        return condition;
    }
    // setters
    public void setBrand(String brand) {
        this.brand = brand;
    }
    public void setMileage(int mileage) {
        this.mileage = mileage;
    }
    public void setCondition(String condition) {
        this.condition = condition;
    }
}
