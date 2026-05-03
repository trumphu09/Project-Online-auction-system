package com.auction.server.models;

import java.time.LocalDateTime;

public class Electronics extends Item {

    // Thuộc tính final: Bắt buộc phải gán giá trị trong Constructor
    private final int warrantyMonths;

    // 1. Constructor số 1: Dùng khi có đầy đủ thông tin (Thêm mới từ đầu)
    public Electronics(int id, int sellerId, String name, String description, double startingPrice, int warrantyMonths, LocalDateTime startTime, LocalDateTime endTime) {
        super(id, sellerId, name, description, startingPrice, startTime, endTime, "ELECTRONICS");
        this.warrantyMonths = warrantyMonths;
    }

    // 2. Constructor số 2: Dùng khi lấy từ Database lên (Có đủ cả status, currentPrice...)
    public Electronics(int id, int sellerId, String name, String description, double startingPrice, double currentPrice, LocalDateTime startTime, LocalDateTime endTime, AuctionStatus status, int highestBidderId, int warrantyMonths) {
        super(id, sellerId, name, description, startingPrice, currentPrice, startTime, endTime, status, highestBidderId, "ELECTRONICS");
        this.warrantyMonths = warrantyMonths;
    }

    // 3. Constructor số 3: DÀNH RIÊNG CHO CÁI API CREATE ITEM
    public Electronics(int id, int sellerId, String name, String description, double startingPrice, LocalDateTime startTime, LocalDateTime endTime, String category) {
        // Đẩy 8 tham số cơ bản lên cho class cha (Item)
        super(id, sellerId, name, description, startingPrice, startTime, endTime, category);

        // Gán giá trị mặc định cho thuộc tính final
        this.warrantyMonths = 0; // Mặc định là 0 tháng bảo hành nếu chưa có dữ liệu
    }

    @Override
    public void printInfo() {
        System.out.println("Sản phẩm điện tử: " + getName() + " | Bảo hành: " + warrantyMonths + " tháng");
    }

    // ===== GETTERS =====
    public int getWarrantyMonths() {
        return warrantyMonths;
    }

    // KHÔNG CÓ SETTER vì thuộc tính là final (Thiết kế như này là rất chặt chẽ, tốt!)
}