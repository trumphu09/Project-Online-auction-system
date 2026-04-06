package com.auction.server.models;
import java.time.LocalDateTime;
public class Electronics extends com.auction.server.models.Item {
    private final int warrantyMonths;
    public Electronics(int id, String name, String description, double startingPrice, int warrantyMonths, LocalDateTime startTime, LocalDateTime endTime) {
        super(id, name, description, startingPrice, startTime, endTime, "ELECTRONICS");
        this.warrantyMonths = warrantyMonths;
    }

    @Override
    public void printInfo() {
        System.out.println("Sản phẩm điện tử: " + getName() + " | Bảo hành: " + warrantyMonths + " tháng");
    }
}
