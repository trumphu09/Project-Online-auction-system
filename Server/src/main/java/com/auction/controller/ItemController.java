package com.auction.controller;

import com.auction.server.factory.ItemFactory;
import com.auction.server.models.ItemDTO;
import com.auction.server.dao.ItemDAO;
import com.auction.service.ItemService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemController {

    private final Gson gson;
    private final ItemService itemService;

    public ItemController() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.itemService = ItemService.getInstance();
    }

    /**
     * FIX: handleListItems — Đảm bảo trả về List thẳng trong "data" (không phải Map lồng).
     * Client mong đợi "data" là một mảng JSON [...], không phải object {"items":[...],...}.
     */
    public String handleListItems(int page, int limit) {
        try {
            Map<String, Object> serviceResult = itemService.getAllItems(page, limit);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("data", serviceResult.get("items"));   // chỉ mảng items
            response.put("pagination", Map.of(
                    "totalItems", serviceResult.get("totalItems"),
                    "totalPages", serviceResult.get("totalPages"),
                    "currentPage", serviceResult.get("currentPage")
            ));

            return gson.toJson(response);

        } catch (Exception e) {
            System.err.println("Lỗi handleListItems: " + e.getMessage());
            JsonObject error = new JsonObject();
            error.addProperty("status", "error");
            error.addProperty("message", "Lỗi tải danh sách sản phẩm: " + e.getMessage());
            return gson.toJson(error);
        }
    }
 
    /**
     * FIX: handleSearchItems trước đây trả về dữ liệu không đúng format
     * hoặc gọi ItemService.searchItems() vốn trả về HashMap rỗng.
     * 
     * Giờ gọi đúng ItemService.searchItems(keyword) → trả về List<ItemDTO>.
     */
    public String handleSearchItems(String keyword, int page, int limit) {
        try {
            // Gọi phương thức search đã được sửa trong ItemService
            List<ItemDTO> items = ItemService.getInstance().searchItems(keyword);
 
            // Bọc vào format chuẩn {"status":"success","data":[...]}
            JsonObject response = new JsonObject();
            response.addProperty("status", "success");
            response.add("data", gson.toJsonTree(items));
            return gson.toJson(response);
 
        } catch (Exception e) {
            System.err.println("Lỗi handleSearchItems: " + e.getMessage());
            JsonObject error = new JsonObject();
            error.addProperty("status", "error");
            error.addProperty("message", "Lỗi tìm kiếm sản phẩm: " + e.getMessage());
            return gson.toJson(error);
        }
    }
 
    public String handleGetCategories() {
        List<String> categories = itemService.getAllCategories();
        return createResponse("success", "Lấy danh sách danh mục thành công.", gson.toJsonTree(categories));
    }

    /**
     * FIX: handleGetItemsByCategory trước đây gọi ItemService.getItemsByCategory()
     * vốn trả về HashMap rỗng.
     *
     * Giờ gọi đúng và trả về List<ItemDTO>.
     */
    public String handleGetItemsByCategory(String category, int page, int limit) {
        try {
            List<ItemDTO> items = ItemService.getInstance().getItemsByCategory(category);
 
            JsonObject response = new JsonObject();
            response.addProperty("status", "success");
            response.add("data", gson.toJsonTree(items));
            return gson.toJson(response);
 
        } catch (Exception e) {
            System.err.println("Lỗi handleGetItemsByCategory: " + e.getMessage());
            JsonObject error = new JsonObject();
            error.addProperty("status", "error");
            error.addProperty("message", "Lỗi lọc danh mục: " + e.getMessage());
            return gson.toJson(error);
        }
    }

    public String handleGetItemsBySeller(int sellerId) {
        List<ItemDTO> items = itemService.getItemsBySeller(sellerId);
        return createResponse("success", "Lấy danh sách sản phẩm của người bán thành công.", gson.toJsonTree(items));
    }

    public String handleGetWonItems(int userId) {
        List<ItemDTO> items = itemService.getWonItems(userId);
        return createResponse("success", "Lấy danh sách sản phẩm đã thắng thành công.", gson.toJsonTree(items));
    }

    public String handleAddItem(String jsonRequest, int sellerId) {
        try {
            JsonObject rawJson = gson.fromJson(jsonRequest, JsonObject.class);
            if (rawJson == null || !rawJson.has("category")) {
                return createResponse("error", "Thất bại: JSON bắt buộc phải có trường 'category'!", null);
            }
            String category = rawJson.get("category").getAsString().toUpperCase();
            Class<? extends ItemDTO> targetClass = ItemFactory.getClassByCategory(category);
            if (targetClass == null) {
                return createResponse("error", "Thất bại: Hệ thống chưa hỗ trợ danh mục này (" + category + ")", null);
            }
            ItemDTO newItem = gson.fromJson(jsonRequest, targetClass);
            newItem.setSellerId(sellerId);
            String resultMessage = itemService.addNewItem(newItem);
            if (resultMessage.startsWith("Thành công")) {
                return createResponse("success", resultMessage, null);
            } else {
                return createResponse("error", resultMessage, null);
            }
        } catch (JsonSyntaxException e) {
            return createResponse("error", "Thất bại: JSON sai cú pháp!", null);
        } catch (Exception e) {
            e.printStackTrace();
            return createResponse("error", "Lỗi server không xác định.", null);
        }
    }

    public String handleUpdateItem(String jsonRequest, int itemId, int sellerId) {
        try {
            JsonObject rawJson = gson.fromJson(jsonRequest, JsonObject.class);
            if (rawJson == null || !rawJson.has("category")) {
                return createResponse("error", "Thất bại: JSON bắt buộc phải có trường 'category'!", null);
            }
            String category = rawJson.get("category").getAsString().toUpperCase();
            Class<? extends ItemDTO> targetClass = ItemFactory.getClassByCategory(category);
            if (targetClass == null) {
                return createResponse("error", "Thất bại: Danh mục không hợp lệ.", null);
            }
            ItemDTO updatedItem = gson.fromJson(jsonRequest, targetClass);
            updatedItem.setId(itemId);
            updatedItem.setSellerId(sellerId);
            String resultMessage = itemService.updateItem(updatedItem);
            if (resultMessage.startsWith("Thành công")) {
                return createResponse("success", resultMessage, null);
            } else {
                return createResponse("error", resultMessage, null);
            }
        } catch (JsonSyntaxException e) {
            return createResponse("error", "Thất bại: JSON sai cú pháp!", null);
        } catch (Exception e) {
            e.printStackTrace();
            return createResponse("error", "Lỗi server không xác định.", null);
        }
    }

    public String handleDeleteItem(int itemId) {
        String resultMessage = itemService.deleteItem(itemId);
        if (resultMessage.startsWith("Thành công")) {
            return createResponse("success", resultMessage, null);
        } else {
            return createResponse("error", resultMessage, null);
        }
    }

    public String handleGetItemDetails(int itemId) {
        try {
            ItemDTO item = itemService.getItemDetails(itemId);
            if (item == null) {
                return createResponse("error", "Không tìm thấy sản phẩm nào có ID là " + itemId, null);
            }
            return createResponse("success", "Lấy thông tin thành công", gson.toJsonTree(item));
        } catch (Exception e) {
            e.printStackTrace();
            return createResponse("error", "Lỗi server khi lấy chi tiết sản phẩm.", null);
        }
    }

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