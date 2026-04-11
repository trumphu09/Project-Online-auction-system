package org.example.model;

import java.io.Serializable;

/**
 * DTO (Data Transfer Object) để chuyển dữ liệu sản phẩm từ Client lên Server
 */
public class ItemDTO implements Serializable {
    private static final long serialVersionUID = 1L; // Đảm bảo tính ổn định khi truyền qua mạng

    private String name;
    private double startingPrice;
    private double priceStep;
    private String description;
    private String imagePath;
    private int sellerId;

    // 1. Constructor không đối số (Rất cần thiết cho các thư viện xử lý dữ liệu)
    public ItemDTO() {
    }

    // 2. Constructor đầy đủ để Đại gọi nhanh trong Controller
    public ItemDTO(String name, double startingPrice, double priceStep, String description, String imagePath, int sellerId) {
        this.name = name;
        this.startingPrice = startingPrice;
        this.priceStep = priceStep;
        this.description = description;
        this.imagePath = imagePath;
        this.sellerId = sellerId;
    }

    // 3. Các Getter và Setter (Để lấy và gán dữ liệu)
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getStartingPrice() { return startingPrice; }
    public void setStartingPrice(double startingPrice) { this.startingPrice = startingPrice; }

    public double getPriceStep() { return priceStep; }
    public void setPriceStep(double priceStep) { this.priceStep = priceStep; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public int getSellerId() { return sellerId; }
    public void setSellerId(int sellerId) { this.sellerId = sellerId; }

    // 4. Hàm toString để Đại dễ dàng in ra console lúc Debug
    @Override
    public String toString() {
        return "ItemDTO{" +
                "name='" + name + '\'' +
                ", price=" + startingPrice +
                ", sellerId=" + sellerId +
                '}';
    }
}
