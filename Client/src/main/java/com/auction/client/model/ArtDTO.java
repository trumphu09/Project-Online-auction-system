package com.auction.client.model;

public class ArtDTO extends ItemDTO {
    public ArtDTO() {
        super();
    }

    public ArtDTO(String name, double startingPrice, double priceStep,
                  String description, String imagePath, int sellerId,
                  String startTime, String endTime) {
        super(name, startingPrice, priceStep, description, imagePath, sellerId, startTime, endTime);
    }

    @Override
    public String getCategory() {
        return "Art";
    }
}
