package com.auction.server.factory;

import com.auction.server.models.*;
import java.util.HashMap;
import java.util.Map;

public class ItemFactory {
    
    private static final Map<String, Class<? extends ItemDTO>> registry = new HashMap<>();

    static {
        registry.put("ART", ArtDTO.class);
        registry.put("ELECTRONICS", ElectronicsDTO.class);
        registry.put("VEHICLE", VehicleDTO.class);
    }

    public static void registerItemType(String category, Class<? extends ItemDTO> clazz) {
        registry.put(category.toUpperCase(), clazz);
    }

    public static Class<? extends ItemDTO> getClassByCategory(String category) {
        if (category == null) {
            return null;
        }
        return registry.get(category.toUpperCase());
    }
}