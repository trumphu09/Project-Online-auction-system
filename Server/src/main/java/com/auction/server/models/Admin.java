package com.auction.server.models;

public class Admin extends User {

    private final String adminRole;

    public Admin(int id, String username, String password, String email, String adminRole) {
        super(id, username, password, email, UserRole.ADMIN);
        this.adminRole = adminRole;
    }

    public void cancelAuction(Auction targetAuction) {
        targetAuction.setStatus(AuctionStatus.CANCELED);
    }

    public void banUser(User user) {
        user.setActive(false);
    }

    public void unbanUser(User user) {
        user.setActive(true);
    }

    @Override
    public void showDashboard() {
        System.out.println("--- Bảng Điều Khiển Quản Trị Viên ---");
        System.out.println("Xin chào sếp: " + getUsername() + " (Quyền: " + adminRole + ")");
    }
}