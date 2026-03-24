package com.auction.server.models;
public abstract class Item {
    private final String id;
    private final String name;
    private final String description;
    private final double startingPrice;

    public Item(String id, String name, String description, double startingPrice) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
    }

    public double getStartingPrice() { return startingPrice; }
    public String getName() { return name; }

    public abstract void printInfo();

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }
}