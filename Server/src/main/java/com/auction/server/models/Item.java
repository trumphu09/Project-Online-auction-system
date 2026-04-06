package com.auction.server.models;
import java.time.LocalDateTime;
public abstract class Item extends Entity {
    private final String name;
    private final String description;
    private final double startingPrice;
    private int sellerId;
    private double currentMaxPrice;
    private int highestBidderId;
    private AuctionStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String category;

    public Item(int id, String name, String description, double startingPrice, LocalDateTime startTime, LocalDateTime endTime, String category) {
        super(id);
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
        this.currentMaxPrice = startingPrice;
        this.highestBidderId = -1; // -1 nghĩa là chưa có ai đặt giá
        this.status = AuctionStatus.OPEN;
        this.startTime = startTime;
        this.endTime = endTime;
        this.category = category;
    }

    public double sethighestBid(double newBid, int bidderId) {
        if (newBid > currentMaxPrice && status == AuctionStatus.RUNNING) {
            currentMaxPrice = newBid;
            highestBidderId = bidderId;
            return currentMaxPrice;
        }
        return -1; // Trả về -1 nếu giá không hợp lệ
    }

    // --- CONSTRUCTOR CHO DAO LẤY LÊN TỪ DATABASE ---
    public Item(int id, int sellerId, String name, String description, double startingPrice, double currentPrice, LocalDateTime startTime, LocalDateTime endTime, AuctionStatus status) {
        this.id = id;
        this.sellerId = sellerId;
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
        this.currentMaxPrice = currentPrice;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    public double getCurrentMaxPrice() { return currentMaxPrice; }
    public int getHighestBidderId() { return highestBidderId; }
    public AuctionStatus getStatus() { return status; }
    public void setStatus(AuctionStatus status) { this.status = status; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public double getStartingPrice() { return startingPrice; }
    public String getName() { return name; }
    public int getSellerId() { return sellerId; }
    public String getCategory() { return category; }
    public void setHighestBidderId(int bidderId) { this.highestBidderId = bidderId; }
    public void setCurrentMaxPrice(double newPrice) { this.currentMaxPrice = newPrice;}
    public void setCategory(String category) { this.category = category; }
    public abstract void printInfo();

    public String getDescription() {
        return description;
    }
}