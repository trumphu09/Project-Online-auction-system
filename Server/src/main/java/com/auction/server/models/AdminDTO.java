package com.auction.server.models;
//DataTransferObject for Admin, used to send admin info to clients without exposing sensitive data like password
public class AdminDTO {
    private int id;
    private String username;
    private String email;
    private String adminRole;

    public AdminDTO(int id, String username, String email, String adminRole) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.adminRole = adminRole;
    }

    // Getters only
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getAdminRole() { return adminRole; }
}