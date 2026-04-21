package com.auction.server.factory;
import com.auction.server.models.*;
import java.util.HashMap;
import java.util.Map;

public class ItemFactory {

    // Đây là "Bản đồ" lưu trữ mối quan hệ giữa chuỗi Category và Class tương ứng
    private static final Map<String, Class<? extends ItemDTO>> registry = new HashMap<>();

    // Khối static này sẽ chạy ngay khi chương trình vừa khởi động
    static {
        registry.put("ART", ArtDTO.class);
        registry.put("ELECTRONICS", ElectronicsDTO.class);
        registry.put("VEHICLE", VehicleDTO.class);
        
        // 💡 TƯƠNG LAI: Nếu có thêm Bất động sản, bạn CHỈ CẦN thêm 1 dòng duy nhất ở đây:
        // registry.put("REAL_ESTATE", RealEstateDTO.class);
    }

    /**
     * Hàm này nhận vào một chữ (ví dụ "VEHICLE") 
     * và trả về đúng cái Khuôn (Class) cần thiết để Gson đúc ra DTO.
     */
    public static Class<? extends ItemDTO> getClassByCategory(String category) {
        if (category == null) return null;
        return registry.get(category.toUpperCase());
    }
}

