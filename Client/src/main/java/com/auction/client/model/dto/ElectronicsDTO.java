package com.auction.client.model.dto;

public class ElectronicsDTO extends ItemDTO {
    // Thêm trường riêng của bảng electronics
    private int warrantyMonths;

    public ElectronicsDTO() { super(); }

    public ElectronicsDTO(String name, double startingPrice, double priceStep, String description, String imagePath, int sellerId, String startTime, String endTime) {
        super(name, startingPrice, priceStep, description, imagePath, sellerId, startTime, endTime);
    }

    @Override
    public String getCategory() { return "Electronics"; }

    public int getWarrantyMonths() { return warrantyMonths; }
    public void setWarrantyMonths(int warrantyMonths) { this.warrantyMonths = warrantyMonths; }
}