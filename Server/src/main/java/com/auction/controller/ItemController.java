package com.auction.controller;

import com.auction.server.factory.ItemFactory;
import com.auction.server.models.ItemDTO;
import com.auction.service.ItemService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

public class ItemController {

    private final Gson gson;
    private final ItemService itemService;

    public ItemController() {
        // Cấu hình Gson in ra JSON có thụt lề cho đẹp (PrettyPrinting)
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.itemService = ItemService.getInstance();
    }

    // ==========================================
    // 1. API: THÊM MỚI SẢN PHẨM (CREATE)
    // ==========================================
    public String handleAddItem(String jsonRequest) {
        try {
            // Bước 1: Bóc tạm lớp vỏ JSON để "soi" xem người dùng gửi loại hàng gì
            JsonObject rawJson = gson.fromJson(jsonRequest, JsonObject.class);
            
            if (!rawJson.has("category")) {
                return createResponse("error", "Thất bại: JSON bắt buộc phải có trường 'category' (ART, ELECTRONICS, VEHICLE)!", null);
            }

            // Lấy chữ category (ví dụ: "VEHICLE")
            String category = rawJson.get("category").getAsString().toUpperCase();

            // ---------------------------------------------------------
            // SỨC MẠNH CỦA FACTORY PATTERN: Không còn một lệnh switch-case nào!
            // Nhờ Nhà máy tìm đúng Class tương ứng (Ví dụ tìm ra VehicleDTO.class)
            // ---------------------------------------------------------
            Class<? extends ItemDTO> targetClass = ItemFactory.getClassByCategory(category);
            
            if (targetClass == null) {
                return createResponse("error", "Thất bại: Hệ thống chưa hỗ trợ danh mục sản phẩm này (" + category + ")", null);
            }

            // Bước 2: Bóc toàn bộ JSON đúc vào đúng cái khuôn (Class) vừa tìm được
            ItemDTO newItem = gson.fromJson(jsonRequest, targetClass);

            // Giao cho Service kiểm tra và lưu xuống Database
            String resultMessage = itemService.addNewItem(newItem);

            // Trả kết quả chữ về cho Frontend
            if (resultMessage.startsWith("Thành công")) {
                return createResponse("success", resultMessage, null);
            } else {
                return createResponse("error", resultMessage, null);
            }

        } catch (JsonSyntaxException e) {
            return createResponse("error", "Thất bại: JSON sai cú pháp hoặc sai kiểu dữ liệu!", null);
        } catch (Exception e) {
            return createResponse("error", "Lỗi server: " + e.getMessage(), null);
        }
    }

    // ==========================================
    // 2. API: XEM CHI TIẾT SẢN PHẨM (READ)
    // ==========================================
    public String handleGetItemDetails(int itemId) {
        try {
            // Gọi Service lấy dữ liệu (Service lại gọi ItemDAO.getItemById)
            ItemDTO item = itemService.getItemDetails(itemId);

            if (item == null) {
                return createResponse("error", "Không tìm thấy sản phẩm nào có ID là " + itemId, null);
            }

            // SỨC MẠNH CỦA GSON VÀ ĐA HÌNH:
            // Lúc này biến 'item' có thể là ArtDTO, VehicleDTO,... 
            // Cứ ném thẳng vào toJsonTree, Gson tự động biết móc ra các biến như artist, brand, mileage... để tạo JSON!
            return createResponse("success", "Lấy thông tin thành công", gson.toJsonTree(item));

        } catch (Exception e) {
            return createResponse("error", "Lỗi server khi lấy chi tiết sản phẩm: " + e.getMessage(), null);
        }
    }

    // ==========================================
    // HÀM TIỆN ÍCH: TẠO CHUẨN JSON TRẢ VỀ
    // ==========================================
    private String createResponse(String status, String message, com.google.gson.JsonElement data) {
        JsonObject response = new JsonObject();
        response.addProperty("status", status);
        response.addProperty("message", message);
        if (data != null) {
            response.add("data", data);
        }
        return gson.toJson(response);
    }
}