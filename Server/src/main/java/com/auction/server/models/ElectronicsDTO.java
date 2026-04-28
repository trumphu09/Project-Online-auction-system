package com.auction.server.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Lớp DTO dành riêng cho các sản phẩm điện tử.
 * Kế thừa toàn bộ thông tin cơ bản từ ItemDTO.
 */
public class ElectronicsDTO extends ItemDTO {

    @Expose
    @SerializedName("warranty_months")
    private int warrantyMonths;

    public ElectronicsDTO(int id, int sellerId, String name, String description, double startingPrice, 
                          int warrantyMonths) {
        super(id, sellerId, name, description, startingPrice, "ELECTRONICS");
        
        this.warrantyMonths = warrantyMonths;
    }

    public int getWarrantyMonths() {
        return warrantyMonths;
    }

    public void setWarrantyMonths(int warrantyMonths) {
        this.warrantyMonths = warrantyMonths;
    }
}
