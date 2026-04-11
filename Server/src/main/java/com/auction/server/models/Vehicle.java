package com.auction.server.models;

import java.time.LocalDateTime;

public class Vehicle extends Item {
    private String brand;
    private int mileage;
    private String condition;

    // 1. Constructor số 1: Dùng khi có đầy đủ 10 thông tin (Thêm mới từ đầu)
    public Vehicle(int id, int sellerId, String name, String description, double startingPrice, LocalDateTime startTime, LocalDateTime endTime,
                   String brand, int mileage, String condition) {
        super(id, sellerId, name, description, startingPrice, startTime, endTime, "VEHICLE");
        this.brand = brand;
        this.mileage = mileage;
        this.condition = condition;
    }

    // 2. Constructor số 2: Dùng khi lấy từ Database lên (Có đủ cả status, currentPrice...)
    public Vehicle(int id, int sellerId, String name, String description, double startingPrice, double currentPrice, LocalDateTime startTime, LocalDateTime endTime, AuctionStatus status, int highestBidderId,
                   String brand, int mileage, String condition) {
        super(id, sellerId, name, description, startingPrice, currentPrice, startTime, endTime, status, highestBidderId, "VEHICLE");
        this.brand = brand;
        this.mileage = mileage;
        this.condition = condition;
    }

    // 3. Constructor số 3: DÀNH RIÊNG CHO CÁI API CREATE ITEM LÚC NÃY
    // API lúc nãy chỉ gửi 8 thông tin cơ bản, chưa có brand, mileage, condition
    public Vehicle(int id, int sellerId, String name, String description, double startingPrice, LocalDateTime startTime, LocalDateTime endTime, String category) {
        super(id, sellerId, name, description, startingPrice, startTime, endTime, category);
        // Tạm thời gán mặc định cho 3 thuộc tính riêng của xe
        this.brand = "Chưa cập nhật";
        this.mileage = 0;
        this.condition = "Chưa cập nhật";
    }

    @Override
    public void printInfo() {
        System.out.println("--- Phương tiện di chuyển ---");
        System.out.println("Xe: " + brand + " " + getName());
        System.out.println("Tình trạng: " + condition + " | Số km đã đi: " + mileage + " km");
        System.out.println("Giá khởi điểm: $" + getStartingPrice());
    }

    // --- GETTERS ---
    public String getBrand() {
        return brand;
    }
    public int getMileage() {
        return mileage;
    }
    public String getCondition() {
        return condition;
    }

    // --- SETTERS ---
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