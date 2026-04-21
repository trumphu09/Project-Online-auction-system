package com.auction.client.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ElectronicsDTO extends ItemDTO {
<<<<<<< HEAD
    @Expose @SerializedName("warranty_months")
    private int warrantyMonths;

    public ElectronicsDTO(int id, int sellerId, String name, String description, double startingPrice, int warrantyMonths) {
        super(id, sellerId, name, description, startingPrice, "ELECTRONICS");
        this.warrantyMonths = warrantyMonths;
    }

=======
    // Thêm trường riêng của bảng electronics
    private int warrantyMonths;

    public ElectronicsDTO() { super(); }

    public ElectronicsDTO(String name, double startingPrice, double priceStep, String description, String imagePath, int sellerId, String startTime, String endTime) {
        super(name, startingPrice, priceStep, description, imagePath, sellerId, startTime, endTime);
    }

    @Override
    public String getCategory() { return "Electronics"; }

>>>>>>> 71e6ab4a2b9c335e64205860a7a9ead1080a473c
    public int getWarrantyMonths() { return warrantyMonths; }
    public void setWarrantyMonths(int warrantyMonths) { this.warrantyMonths = warrantyMonths; }
}