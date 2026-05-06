package com.auction.client.model.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ArtDTO extends ItemDTO {
    @Expose @SerializedName("artist")
    private String artist;

    @Expose @SerializedName("creation_year")
    private int creationYear;

    @Expose @SerializedName("material")
    private String material;

    public ArtDTO() {
        this.setCategory("ART");
    }

    public ArtDTO(int id, int sellerId, String name, String description, double startingPrice, 
                  String artist, int creationYear, String material) {
        super(id, sellerId, name, description, startingPrice, "ART");
        this.artist = artist;
        this.creationYear = creationYear;
        this.material = material;
    }

    // Getters & Setters
    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }
    public int getCreationYear() { return creationYear; }
    public void setCreationYear(int creationYear) { this.creationYear = creationYear; }
    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }
}