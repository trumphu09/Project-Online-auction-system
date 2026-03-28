package com.auction.server.models;

public abstract class Entity {
    protected int id;

    public Entity(){
        this.id = 0;
    }
    public Entity(int id) {
        this.id = id;
    }

    public int getId(){
        return id;
    } 

    public void setId(int id){
        this.id = id;
    }

}
 