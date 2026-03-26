package com.auction.server.models;

public abstract class Entity {
    protected final String id;

    public Entity(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
