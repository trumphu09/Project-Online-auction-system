package com.auction.client.model.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ElectronicsDTO extends ItemDTO {
    @Expose @SerializedName("warranty_months")
    private int warrantyMonths;

    public ElectronicsDTO() {
        this.setCategory("ELECTRONICS");
    }
    
    public ElectronicsDTO(int id, int sellerId, String name, String description, double startingPrice, int warrantyMonths) {
        super(id, sellerId, name, description, startingPrice, "ELECTRONICS");
        this.warrantyMonths = warrantyMonths;
    }

    public int getWarrantyMonths() { return warrantyMonths; }
    public void setWarrantyMonths(int warrantyMonths) { this.warrantyMonths = warrantyMonths; }
}