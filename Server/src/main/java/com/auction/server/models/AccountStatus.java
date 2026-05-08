package com.auction.server.models;

public enum AccountStatus {
    ACTIVE,     // Đang hoạt động
    INACTIVE,   // Không hoạt động (tự nguyện hoặc do admin)
    BANNED,     // Bị cấm vĩnh viễn
    SUSPENDED;  // Bị treo tài khoản tạm thời

    public static AccountStatus fromString(String text) {
        if (text != null) {
            for (AccountStatus status : AccountStatus.values()) {
                if (text.equalsIgnoreCase(status.name())) {
                    return status;
                }
            }
        }
        return INACTIVE; // Trạng thái mặc định an toàn
    }
}