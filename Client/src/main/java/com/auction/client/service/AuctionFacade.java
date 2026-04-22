package com.auction.client.service;

import com.auction.client.api.ApiService;
import com.auction.client.model.User;
import com.auction.client.model.dto.ItemDTO;
import com.google.gson.Gson;
import javafx.application.Platform;

public class AuctionFacade {
    private final ApiService apiService = ApiService.getInstance();
    private final Gson gson = new Gson();

    // HÀM CHUNG CHO USER (Register/Login)
    public void handleUserAction(String endpoint, User user, ApiCallback callback) {
        String json = gson.toJson(user);

        apiService.sendPostRequest(endpoint, json)
                .thenAccept(response -> {
                    // Xử lý logic chung ở đây, sau đó đẩy về Controller
                    Platform.runLater(() -> callback.onSuccess(response));
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> callback.onError(ex.getMessage()));
                    return null;
                });
    }

    // HÀM CHUNG CHO ITEM (Post/Update)
    public void handleItemAction(ItemDTO item, ApiCallback callback) {
        String json = gson.toJson(item);

        apiService.sendPostRequest("/items/add", json)
                .thenAccept(response -> {
                    Platform.runLater(() -> callback.onSuccess(response));
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> callback.onError(ex.getMessage()));
                    return null;
                });
    }
}