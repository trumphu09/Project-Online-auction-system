package com.auction.server.models;
public class Art extends Item {

    private final String artist;
    private final int creationYear;
    private final String material;

    public Art(String id, String name, String description, double startingPrice,
               String artist, int creationYear, String material) {
        super(id, name, description, startingPrice);
        this.artist = artist;
        this.creationYear = creationYear;
        this.material = material;
    }

    @Override
    public void printInfo() {
        System.out.println("--- Tác phẩm nghệ thuật ---");
        System.out.println("Tên tác phẩm: " + getName() + " | Tác giả: " + artist);
        System.out.println("Năm: " + creationYear + " | Chất liệu: " + material);
        System.out.println("Giá khởi điểm: $" + getStartingPrice());
    }
}