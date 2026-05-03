package com.auction.server.models;

import java.time.LocalDateTime;

public class Art extends Item {

    private String artist;
    private int creation_year;
    private String material;

    public Art(int id,int sellerId, String name, String description, double startingPrice, LocalDateTime startTime, LocalDateTime endTime,
               String artist, int creation_year, String material) {
        super(id,sellerId, name, description, startingPrice, startTime, endTime, "ART" );
        this.artist = artist;
        this.creation_year = creation_year;
        this.material = material;
    }
    // constructor co day du tat ca thuoc tinh
    public Art(int id,int sellerId, String name, String description, double startingPrice, double currentPrice, LocalDateTime startTime, LocalDateTime endTime, AuctionStatus status, int highestBidderId,
               String artist, int creation_year, String material) {
        super(id,sellerId, name, description, startingPrice, currentPrice, startTime, endTime, status, highestBidderId,"ART" );
        this.artist = artist;
        this.creation_year = creation_year;
        this.material = material;
    }

    // Constructor số 3: DÀNH RIÊNG CHO CÁI API CREATE ITEM
    public Art(int id, int sellerId, String name, String description, double startingPrice, LocalDateTime startTime, LocalDateTime endTime, String category) {
        // 1. Đẩy 8 tham số cơ bản lên cho class cha (Item)
        super(id, sellerId, name, description, startingPrice, startTime, endTime, category);

        // 2. Gán giá trị mặc định cho các thuộc tính riêng của tác phẩm nghệ thuật
        this.artist = "Chưa cập nhật";
        this.creation_year = 0;
        this.material = "Chưa cập nhật";
    }

    public String getArtist() {
        return artist;
    }
    public int getCreationYear() {
        return creation_year;
    }
    public String getMaterial() {
        return material;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }
    public void setCreationYear(int creation_year) {
        this.creation_year = creation_year;
    }
    public void setMaterial(String material) {
        this.material = material;
    }
    @Override
    public void printInfo() {
        System.out.println("--- Tác phẩm nghệ thuật ---");
        System.out.println("Tên tác phẩm: " + getName() + " | Tác giả: " + artist);
        System.out.println("Năm: " + creation_year + " | Chất liệu: " + material);
        System.out.println("Giá khởi điểm: $" + getStartingPrice());
    }
}