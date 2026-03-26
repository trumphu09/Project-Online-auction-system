package com.auction.server.models;
public class Admin extends User {

    // Thuộc tính riêng: Phân cấp quyền (ví dụ: SuperAdmin, Moderator)
    private final String adminRole;

    public Admin(String id, String username, String password, String email, String adminRole) {
        super(id, username, password, email);
        this.adminRole = adminRole;
    }

    // Hành động riêng: Can thiệp vào hệ thống
    // (Lưu ý: Để hàm này chạy được, trong class Auction bạn cần viết thêm hàm setStatus("CANCELED"))
    // Trong file Admin.java
    public void cancelAuction(Auction targetAuction) {
        System.out.println("CẢNH BÁO: Admin [" + getUsername() + "] đã cưỡng chế HỦY phiên đấu giá!");

        // Cú pháp dùng Enum: TênEnum.GIA_TRI
        targetAuction.setStatus(AuctionStatus.CANCELED);
    }

<<<<<<< HEAD
    public void banUser(User user) {
=======
    public void banUser(com.auction.server.models.User user) {
        user.setActive(false);
>>>>>>> 85ba655fc51de033a36d9a4f780f43763fc41b25
        System.out.println("Admin [" + getUsername() + "] đã khóa tài khoản của: " + user.getUsername());
    }

    public void unbanUser(User user) {
        user.setActive(true);
        System.out.println("MỞ KHÓA: Admin [" + getUsername() + "] đã khôi phục tài khoản cho: " + user.getUsername());
    }

    // Ghi đè phương thức rỗng của lớp cha
    @Override
    public void showDashboard() {
        System.out.println("--- Bảng Điều Khiển Quản Trị Viên ---");
        System.out.println("Xin chào sếp: " + getUsername() + " (Quyền: " + adminRole + ")");
    }
}