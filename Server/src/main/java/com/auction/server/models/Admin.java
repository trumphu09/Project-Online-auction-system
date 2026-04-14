package com.auction.server.models;
public class Admin extends User {

    // Thuộc tính riêng: Phân cấp quyền (ví dụ: SuperAdmin, Moderator)
    private final String adminRole;

    public Admin(int id, String username, String password, String email, String adminRole) {
        super(id, username, password, email);
        this.adminRole = adminRole;
    }

    // Hành động riêng: Can thiệp vào hệ thống
    // (Lưu ý: Để hàm này chạy được, trong class Auction bạn cần viết thêm hàm setStatus("CANCELED"))
    // Trong file Admin.java
    public void cancelAuction(Auction targetAuction) {
       
        // Cú pháp dùng Enum: TênEnum.GIA_TRI
        targetAuction.setStatus(AuctionStatus.CANCELED);
    }

    public void banUser(User user) {
        user.setActive(false);
    }

    public void unbanUser(User user) {
        user.setActive(true);
    }

    // Ghi đè phương thức rỗng của lớp cha
    @Override
    public void showDashboard() {
        System.out.println("--- Bảng Điều Khiển Quản Trị Viên ---");
        System.out.println("Xin chào sếp: " + getUsername() + " (Quyền: " + adminRole + ")");
    }
}