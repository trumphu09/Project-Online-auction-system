package com.auction.server.factory; // Hoặc thư mục chứa Factory của nhóm

import com.auction.server.models.*;
import java.util.HashMap;
import java.util.Map;

public class ItemFactory {
    
    // BỘ ĐĂNG KÝ (REGISTRY): Lưu trữ từ khóa nhận diện và Class tương ứng
    private static final Map<String, Class<? extends ItemDTO>> registry = new HashMap<>();

    // Khối static chạy 1 lần duy nhất khi hệ thống khởi động
    static {
        // Đăng ký các sản phẩm mặc định của hệ thống
        registry.put("ART", ArtDTO.class);
        registry.put("ELECTRONICS", ElectronicsDTO.class);
        registry.put("VEHICLE", VehicleDTO.class);
    }

    /**
     * TÍNH NĂNG ĐỈNH CAO CỦA OCP: Cho phép đăng ký loại sản phẩm MỚI từ bên ngoài.
     * Ngày mai có RealEstate, em chỉ cần gọi ItemFactory.registerItemType("REAL_ESTATE", RealEstateDTO.class)
     * từ file Main mà KHÔNG CẦN mở file ItemFactory.java này ra để sửa!
     */
    public static void registerItemType(String category, Class<? extends ItemDTO> clazz) {
        registry.put(category.toUpperCase(), clazz);
    }

    /**
     * Dành cho API/Controller: Trả về Class tương ứng để thư viện Gson có thể tự động dịch JSON thành Object
     */
    public static Class<? extends ItemDTO> getClassByCategory(String category) {
        Class<? extends ItemDTO> clazz = registry.get(category.toUpperCase());
        
        if (clazz == null) {
            // Thay vì lỗi ngầm, báo lỗi ngay lập tức nếu mã không tồn tại
            throw new IllegalArgumentException("Loại sản phẩm không được hỗ trợ: " + category);
        }
        
        return clazz;
    }
}