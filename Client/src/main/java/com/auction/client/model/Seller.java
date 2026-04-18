package com.auction.client.model;

public class Seller extends User {
    public Seller(String username, String password) { super(username, password, "SELLER"); }

    @Override
    public String getDashboardFXML() {
        return "/view/ProductView.fxml";
    }
}
