package com.auction.client.model;

public class Admin extends User {
    public Admin(String username, String password) { super(username, password, "ADMIN");}

    @Override
    public String getDashboardFXML() {
        return "/view/AdminView.fxml";
    }
}

