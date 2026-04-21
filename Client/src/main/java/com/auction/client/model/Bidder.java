package com.auction.client.model;

public class Bidder extends User {
    public Bidder(String username, String password) { super(username, password, "BIDDER");}

    @Override
    public String getDashboardFXML() {
        return "/view/ProductView.fxml";
    }
}