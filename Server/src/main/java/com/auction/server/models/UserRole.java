package com.auction.server.models;

public enum UserRole {
    ADMIN,
    SELLER,
    BIDDER,
    INACTIVE;

    /**
     * Chuyển đổi một chuỗi thành UserRole một cách an toàn.
     * @param text Chuỗi đầu vào (ví dụ: "ADMIN", "seller").
     * @return Đối tượng UserRole tương ứng, hoặc INACTIVE nếu không hợp lệ.
     */
    public static UserRole fromString(String text) {
        if (text != null) {
            for (UserRole role : UserRole.values()) {
                if (text.equalsIgnoreCase(role.name())) {
                    return role;
                }
            }
        }
        return INACTIVE; // Trả về giá trị mặc định an toàn
    }
}