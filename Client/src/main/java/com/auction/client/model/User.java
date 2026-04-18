package com.auction.client.model;

import java.io.Serializable;

public abstract class User implements Serializable {
    protected String username;
    protected String password;
    protected String role;
    protected boolean locked;

    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // Getter và Setter chung
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password;}

    public String getRole() { return role; }

    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }

    // Phương thức trừu tượng: Mỗi loại User sẽ có một bảng điều khiển riêng
    public abstract String getDashboardFXML();
}