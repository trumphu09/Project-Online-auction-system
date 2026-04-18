package com.auction.client.model;

public class ArtDTO extends ItemDTO {
    // Thêm các trường riêng của bảng artworks
    private String artist;
    private int creation_year;
    private String material;

    public ArtDTO() { super(); }

    public ArtDTO(String artist, int creation_year, String material) {
        super();
        this.artist = artist;
        this.creation_year = creation_year;
        this.material = material;
    }

    @Override
    public String getCategory() { return "Art"; }

    // Bắt buộc phải có Getter/Setter để Gson chuyển thành JSON
    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }
    public int getCreationYear() { return creation_year; }
    public void setCreationYear(int creation_year) { this.creation_year = creation_year; }
    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }
}