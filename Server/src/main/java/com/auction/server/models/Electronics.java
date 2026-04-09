package com.auction.server.models;
import java.time.LocalDateTime;
public class Electronics extends Item {
    private final int warrantyMonths;
    public Electronics(int id,int sellerId, String name, String description, double startingPrice, int warrantyMonths, LocalDateTime startTime, LocalDateTime endTime) {
        super(id, sellerId, name, description, startingPrice, startTime, endTime, "ELECTRONICS");
        this.warrantyMonths = warrantyMonths;
    }

    // constructor co day du tat ca thuoc tinh
    public Electronics(int id,int sellerId, String name, String description, double startingPrice, double currentPrice, LocalDateTime startTime, LocalDateTime endTime, AuctionStatus status, int highestBidderId,
                       int warrantyMonths) {
        super(id, sellerId, name, description, startingPrice, currentPrice, startTime, endTime, status, highestBidderId,"ELECTRONICS");
        this.warrantyMonths = warrantyMonths;
    }

    @Override
    public void printInfo() {
        System.out.println("Sản phẩm điện tử: " + getName() + " | Bảo hành: " + warrantyMonths + " tháng");
    }

    // ===== GETTERS AND SETTERS FOR ELECTRONICS =====
    
    public int getWarrantyMonths() {
        return warrantyMonths;
    }
   
}
