package com.auction.server.models;
public abstract class Item extends Entity {
    private final String name;
    private final String description;
    private final double startingPrice;

    public Item(String id, String name, String description, double startingPrice) {
        super(id);
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
    }

    public double getStartingPrice() { return startingPrice; }
    public String getName() { return name; }

    public abstract void printInfo();

    public String getDescription() {
        return description;
    }
}