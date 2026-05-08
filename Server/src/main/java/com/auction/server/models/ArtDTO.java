package com.auction.server.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Lớp DTO dành riêng cho các tác phẩm nghệ thuật.
 * Kế thừa toàn bộ thông tin cơ bản từ ItemDTO.
 */
public class ArtDTO extends ItemDTO {

    @Expose
    @SerializedName("artist")
    private String artist;

    // Trong Java nên dùng camelCase (creationYear) thay vì snake_case (creation_year).
    // @SerializedName sẽ lo việc đổi tên thành snake_case khi xuất ra JSON cho Frontend.
    @Expose
    @SerializedName("creation_year")
    private int creationYear;

    @Expose
    @SerializedName("material")
    private String material;

    // ==========================================
    // Constructor
    // ==========================================
    public ArtDTO() {}

    public ArtDTO(int id, int sellerId, String name, String description, double startingPrice, 
                  String artist, int creationYear, String material) {
        super(id, sellerId, name, description, startingPrice, "ART");
        
        this.artist = artist;
        this.creationYear = creationYear;
        this.material = material;
    }
    
    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public int getCreationYear() {
        return creationYear;
    }

    public void setCreationYear(int creationYear) {
        this.creationYear = creationYear;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }
}
