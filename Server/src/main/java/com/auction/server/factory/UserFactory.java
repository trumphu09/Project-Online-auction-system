package com.auction.server.factory;

import com.auction.server.models.Admin;
import com.auction.server.models.Bidder;
import com.auction.server.models.Seller;
import com.auction.server.models.User;
import com.auction.server.models.UserRole;

import java.util.HashMap;
import java.util.Map;

public class UserFactory {
    
    private static final Map<String, Class<? extends User>> registry = new HashMap<>();

    static {
        registry.put("BIDDER", Bidder.class);
        registry.put("SELLER", Seller.class);
        registry.put("ADMIN", Admin.class);
    }

    public static void registerUserType(String role, Class<? extends User> clazz) {
        registry.put(role.toUpperCase(), clazz);
    }

    public static Class<? extends User> getClassByRole(String role) {
        if (role == null) {
            return null;
        }
        return registry.get(role.toUpperCase());
    }

    public static User createUser(String role) throws Exception {
        Class<? extends User> clazz = getClassByRole(role);
        if (clazz != null) {
            return clazz.getDeclaredConstructor().newInstance();
        }
        throw new IllegalArgumentException("Không tìm thấy User Role: " + role);
    }

    // Phương thức tạo User có sẵn dữ liệu từ DB (dành cho SubDAO)
    public static User createUser(UserRole role, int id, String username, String password, String email) {
        if (role == null) return null;
        switch (role) {
            case BIDDER:
                return new Bidder(id, username, password, email);
            case SELLER:
                return new Seller(id, username, password, email);
            case ADMIN:
                // Admin cần thêm tham số role string trong hàm khởi tạo
                return new Admin(id, username, password, email, "ADMIN");
            default:
                throw new IllegalArgumentException("Không hỗ trợ User Role: " + role);
        }
    }
}