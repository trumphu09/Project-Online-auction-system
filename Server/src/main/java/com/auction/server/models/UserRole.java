package com.auction.server.models;

public enum UserRole {
    ADMIN,
    SELLER,
    BIDDER;

    public static UserRole fromString(String text) {
        if (text != null) {
            for (UserRole role : UserRole.values()) {
                if (text.equalsIgnoreCase(role.name())) {
                    return role;
                }
            }
        }
        // Trả về null nếu không tìm thấy, để lớp gọi có thể xử lý
        return null;
    }
}