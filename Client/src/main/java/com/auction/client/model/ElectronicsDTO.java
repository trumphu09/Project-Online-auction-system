package com.auction.client.model;

public class ElectronicsDTO extends ItemDTO {
    // Constructor không tham số (Cần cho Serialization - Tuần 9) [cite: 103]
    public ElectronicsDTO() {
        super();
    }

    // Constructor đầy đủ tham số
    public ElectronicsDTO(String name, double startingPrice, double priceStep,
                          String description, String imagePath, int sellerId,
                          String startTime, String endTime) {
        // Dùng super để đẩy dữ liệu lên lớp cha ItemDTO xử lý
        super(name, startingPrice, priceStep, description, imagePath, sellerId, startTime, endTime);
    }

    @Override
    public String getCategory() {
        return "Electronics";
    }
}