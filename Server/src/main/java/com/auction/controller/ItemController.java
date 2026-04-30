package com.auction.controller;

import com.auction.server.AuctionServer;
import com.auction.server.factory.ItemFactory;
import com.auction.server.models.ItemDTO;
import com.auction.service.ItemService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.List;
import java.util.Map;

public class ItemController {

    private final Gson gson;
    private final ItemService itemService;

    public ItemController() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.itemService = ItemService.getInstance();
    }

    public String handleListItems(int page, int limit) {
        Map<String, Object> data = itemService.getAllItems(page, limit);
        return createResponse("success", "Lấy danh sách sản phẩm thành công.", gson.toJsonTree(data));
    }

    public String handleSearchItems(String keyword, int page, int limit) {
        Map<String, Object> data = itemService.searchItems(keyword, page, limit);
        return createResponse("success", "Kết quả tìm kiếm cho '" + keyword + "'.", gson.toJsonTree(data));
    }

    public String handleGetCategories() {
        List<String> categories = itemService.getAllCategories();
        return createResponse("success", "Lấy danh sách danh mục thành công.", gson.toJsonTree(categories));
    }

    public String handleGetItemsByCategory(String category, int page, int limit) {
        Map<String, Object> data = itemService.getItemsByCategory(category, page, limit);
        return createResponse("success", "Lấy sản phẩm theo danh mục '" + category + "'.", gson.toJsonTree(data));
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
                AuctionServer.getWebSocketServer().broadcastNewItem(newItem);
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
                AuctionServer.getWebSocketServer().broadcastItemUpdated(updatedItem);
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