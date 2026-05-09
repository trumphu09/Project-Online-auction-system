package com.auction.client.service;

import com.auction.client.network.ApiService;
import com.auction.client.model.dto.ItemDTO;
import com.auction.client.model.dto.UserDTO;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;

import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AuctionFacade {
    private static AuctionFacade instance;
    private final ApiService apiService;

    // Chỉ khai báo biến ở đây
    private final com.google.gson.Gson gson;

    // Khởi tạo và cấu hình Gson trong constructor
    private AuctionFacade() {
        this.apiService = ApiService.getInstance();

        this.gson = new com.google.gson.GsonBuilder()
            .registerTypeAdapter(com.auction.client.model.dto.ItemDTO.class,
                new com.google.gson.JsonDeserializer<com.auction.client.model.dto.ItemDTO>() {
                    @Override
                    public com.auction.client.model.dto.ItemDTO deserialize(
                            com.google.gson.JsonElement json,
                            java.lang.reflect.Type typeOfT,
                            com.google.gson.JsonDeserializationContext context) {

                        com.google.gson.JsonObject obj = json.getAsJsonObject();
                        String category = obj.has("category") && !obj.get("category").isJsonNull()
                                ? obj.get("category").getAsString().toUpperCase()
                                : "";

                        com.google.gson.Gson basicGson = new com.google.gson.Gson();

                        if ("ELECTRONICS".equals(category)) {
                            return basicGson.fromJson(json, com.auction.client.model.dto.ElectronicsDTO.class);
                        } else if ("VEHICLE".equals(category)) {
                            return basicGson.fromJson(json, com.auction.client.model.dto.VehicleDTO.class);
                        } else if ("ART".equals(category) || "ARTWORK".equals(category)) {
                            return basicGson.fromJson(json, com.auction.client.model.dto.ArtDTO.class);
                        }

                        return basicGson.fromJson(json, com.auction.client.model.dto.ItemDTO.class);
                    }
                })
            .create();
    }

    public static AuctionFacade getInstance() {
        if (instance == null) {
            instance = new AuctionFacade();
        }
        return instance;
    }

    // =========================================================================
    // 1. AUTH & PROFILE
    // =========================================================================

    public void login(String username, String password, ApiCallback<JsonObject> callback) {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("email", username);
        credentials.put("password", password);
        executeRequest(apiService.sendPostRequest("/login", gson.toJson(credentials)), JsonObject.class, callback);
    }

    public void register(String username, String password, String role, ApiCallback<JsonObject> callback) {
        Map<String, String> data = new HashMap<>();
        String displayUsername = username.contains("@") ? username.split("@")[0] : username;
        data.put("username", displayUsername);
        data.put("password", password);
        data.put("email", username);
        data.put("role", role);
        executeRequest(apiService.sendPostRequest("/register", gson.toJson(data)), JsonObject.class, callback);
    }

    public void logout(ApiCallback<JsonObject> callback) {
        executeRequest(apiService.sendPostRequest("/logout", "{}"), JsonObject.class, callback);
    }

    public void getUserProfile(ApiCallback<UserDTO> callback) {
        executeRequest(apiService.sendGetRequest("/my/profile"), UserDTO.class, callback);
    }

    public void depositMoney(double amount, ApiCallback<JsonObject> callback) {
        JsonObject json = new JsonObject();
        json.addProperty("amount", amount);
        executeRequest(apiService.sendPostRequest("/my/deposit", gson.toJson(json)), JsonObject.class, callback);
    }

    // =========================================================================
    // 2. TÌM KIẾM & XEM SẢN PHẨM (Bidder)
    // =========================================================================

    public void getAllItems(ApiCallback<List<ItemDTO>> callback) {
        executeRequest(
            apiService.sendGetRequest("/items"),
            new TypeToken<List<ItemDTO>>() {}.getType(),
            callback
        );
    }

    public void getMyItems(ApiCallback<List<ItemDTO>> callback) {
        executeRequest(
            apiService.sendGetRequest("/my/items"),
            new TypeToken<List<ItemDTO>>() {}.getType(),
            callback
        );
    }

    public void searchItems(String keyword, ApiCallback<List<ItemDTO>> callback) {
        executeRequest(
            apiService.sendGetRequest("/search/items?q=" + keyword),
            new TypeToken<List<ItemDTO>>() {}.getType(),
            callback
        );
    }

    public void getItemsByCategory(String category, ApiCallback<List<ItemDTO>> callback) {
        executeRequest(
            apiService.sendGetRequest("/categories/" + category),
            new TypeToken<List<ItemDTO>>() {}.getType(),
            callback
        );
    }

    public void getItemDetail(int itemId, ApiCallback<ItemDTO> callback) {
        executeRequest(apiService.sendGetRequest("/items/" + itemId), ItemDTO.class, callback);
    }

    // =========================================================================
    // 3. ĐẤU GIÁ & THANH TOÁN (Bidder)
    // =========================================================================

    public void placeBid(int auctionId, double amount, ApiCallback<JsonObject> callback) {
        JsonObject json = new JsonObject();
        json.addProperty("auction_id", auctionId);
        json.addProperty("amount", amount);
        executeRequest(apiService.sendPostRequest("/bids", gson.toJson(json)), JsonObject.class, callback);
    }

    public void getMyWonItems(ApiCallback<List<ItemDTO>> callback) {
        executeRequest(
            apiService.sendGetRequest("/my/won-items"),
            new TypeToken<List<ItemDTO>>() {}.getType(),
            callback
        );
    }

    public void getMyActiveBids(ApiCallback<List<JsonObject>> callback) {
        executeRequest(
            apiService.sendGetRequest("/my/active-bids"),
            new TypeToken<List<JsonObject>>() {}.getType(),
            callback
        );
    }

    public void processPayment(int auctionId, ApiCallback<JsonObject> callback) {
        JsonObject json = new JsonObject();
        json.addProperty("auction_id", auctionId);
        executeRequest(apiService.sendPostRequest("/payments", gson.toJson(json)), JsonObject.class, callback);
    }

    // =========================================================================
    // 4. SELLER
    // =========================================================================

    public void addItem(ItemDTO newItem, ApiCallback<JsonObject> callback) {
        String jsonBody = gson.toJson(newItem);
        executeRequest(apiService.sendPostRequest("/items", jsonBody), JsonObject.class, callback);
    }

    public void updateItem(ItemDTO item, ApiCallback<JsonObject> callback) {
        if (item == null || item.getId() <= 0) {
            callback.onError("Dữ liệu sản phẩm không hợp lệ!");
            return;
        }
        String jsonBody = gson.toJson(item);
        executeRequest(apiService.sendPutRequest("/items/" + item.getId(), jsonBody), JsonObject.class, callback);
    }

    // =========================================================================
    // 5. ADMIN
    // =========================================================================

    public void getAllUsers(ApiCallback<List<UserDTO>> callback) {
        // FIX: dùng TypeToken đã import thay vì bare reference gây lỗi compile
        Type type = new TypeToken<List<UserDTO>>() {}.getType();
        executeRequest(apiService.sendGetRequest("/admin/users"), type, callback);
    }

    public void updateUserStatus(int userId, String action, ApiCallback<JsonObject> callback) {
        JsonObject json = new JsonObject();
        json.addProperty("action", action);
        executeRequest(apiService.sendPostRequest("/admin/users/" + userId, gson.toJson(json)), JsonObject.class, callback);
    }

    public void adminDeleteItem(int itemId, ApiCallback<JsonObject> callback) {
        executeRequest(apiService.sendDeleteRequest("/admin/items/" + itemId), JsonObject.class, callback);
    }

    // =========================================================================
    // 6. GIỎ HÀNG (Watchlist)
    // =========================================================================

    public void addToWatchlist(int itemId, ApiCallback<JsonObject> callback) {
        JsonObject json = new JsonObject();
        json.addProperty("itemId", itemId);
        executeRequest(apiService.sendPostRequest("/my/watchlist", gson.toJson(json)), JsonObject.class, callback);
    }

    public void getWatchlist(ApiCallback<List<ItemDTO>> callback) {
        executeRequest(
            apiService.sendGetRequest("/my/watchlist"),
            new TypeToken<List<ItemDTO>>() {}.getType(),
            callback
        );
    }

    // =========================================================================
    // HÀM LÕI XỬ LÝ CHUNG
    // =========================================================================
    private <T> void executeRequest(CompletableFuture<HttpResponse<String>> requestFuture,
                                    Type responseType,
                                    ApiCallback<T> callback) {
        requestFuture.thenAccept(response -> {
            Platform.runLater(() -> {
                try {
                    JsonObject rootObj = gson.fromJson(response.body(), JsonObject.class);

                    if (rootObj != null && rootObj.has("status")) {
                        String status = rootObj.get("status").getAsString();
                        if ("error".equals(status)) {
                            String msg = rootObj.has("message")
                                    ? rootObj.get("message").getAsString()
                                    : "Lỗi server";
                            callback.onError(msg);
                            return;
                        }
                        if ("success".equals(status)) {
                            if (rootObj.has("data") && !rootObj.get("data").isJsonNull()) {
                                T result = gson.fromJson(rootObj.get("data"), responseType);
                                callback.onSuccess(result);
                            } else {
                                callback.onSuccess(null);
                            }
                            return;
                        }
                    }
                    callback.onError("Định dạng phản hồi từ Server không chuẩn xác.");
                } catch (Exception ex) {
                    callback.onError("Lỗi hệ thống hoặc parse JSON: " + ex.getMessage());
                }
            });
        }).exceptionally(e -> {
            Platform.runLater(() -> callback.onError("Lỗi mất kết nối máy chủ!"));
            return null;
        });
    }
}