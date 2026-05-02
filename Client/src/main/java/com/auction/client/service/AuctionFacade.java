package com.auction.client.service;

import com.auction.client.api.ApiService;
import com.auction.client.model.dto.ItemDTO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
  private final Gson gson;

  // =========================================================================
  // KHỞI TẠO SINGLETON & GSON
  // =========================================================================
  private AuctionFacade() {
    this.apiService = ApiService.getInstance();
    // Dùng GsonBuilder thay vì new Gson() để sau này dễ nâng cấp (ví dụ: format ngày tháng)
    this.gson = new GsonBuilder().create();
  }

  public static AuctionFacade getInstance() {
    if (instance == null) {
      instance = new AuctionFacade();
    }
    return instance;
  }

  // =========================================================================
  // CÁC HÀM NGHIỆP VỤ (CHỈ MẤT 3-4 DÒNG CHO MỖI CHỨC NĂNG)
  // =========================================================================

  // 1. ĐĂNG NHẬP
  public void login(String username, String password, ApiCallback<JsonObject> callback) {
    Map<String, String> credentials = new HashMap<>();
    credentials.put("username", username);
    credentials.put("password", password);

    executeRequest(apiService.sendPostRequest("/login", gson.toJson(credentials)), JsonObject.class, callback);
  }

  // 2. ĐĂNG KÝ
  public void register(String username, String password, String role, ApiCallback<JsonObject> callback) {
    Map<String, String> data = new HashMap<>();
    data.put("username", username);
    data.put("password", password);
    data.put("role", role);

    executeRequest(apiService.sendPostRequest("/register", gson.toJson(data)), JsonObject.class, callback);
  }

  // 3. LẤY DANH SÁCH SẢN PHẨM (TRANG CHỦ)
  public void getAllItems(ApiCallback<List<ItemDTO>> callback) {
    // Định nghĩa kiểu dữ liệu List<ItemDTO> cho Gson hiểu
    Type listType = new TypeToken<List<ItemDTO>>(){}.getType();

    executeRequest(apiService.sendGetRequest("/items"), listType, callback);
  }

  // 4. ĐĂNG SẢN PHẨM MỚI (DÀNH CHO SELLER)
  public void addItem(ItemDTO newItem, ApiCallback<JsonObject> callback) {
    // Gson tự động biến đối tượng ItemDTO (ArtDTO, VehicleDTO...) thành chuỗi JSON
    String jsonBody = gson.toJson(newItem);

    executeRequest(apiService.sendPostRequest("/items", jsonBody), JsonObject.class, callback);
  }

  // =========================================================================
  // HÀM "LÕI" XỬ LÝ CHUNG (CORE PROCESSOR) - ĐẠI YÊU CẦU
  // =========================================================================
  private <T> void executeRequest(CompletableFuture<HttpResponse<String>> requestFuture, Type responseType, ApiCallback<T> callback) {
    requestFuture.thenAccept(response -> {
      // Luôn cập nhật UI trên luồng chính của JavaFX
      Platform.runLater(() -> {
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
          try {
            // Tự động ép kiểu JSON thành đối tượng mong muốn (JsonObject, List, String...)
            T result = gson.fromJson(response.body(), responseType);
            callback.onSuccess(result);
          } catch (Exception ex) {
            callback.onError("Lỗi phân tích dữ liệu: " + ex.getMessage());
          }
        } else {
          // Tự động trích xuất lời nhắn lỗi từ Server (dựa theo cấu trúc JSON nhóm bạn viết)
          String errorMsg = "Lỗi hệ thống (" + response.statusCode() + ")";
          try {
            JsonObject errObj = gson.fromJson(response.body(), JsonObject.class);
            if (errObj.has("message")) {
              errorMsg = errObj.get("message").getAsString();
            } else if (errObj.has("error")) {
              errorMsg = errObj.get("error").getAsString(); // Bắt lỗi theo chuẩn GetItemDetailAPI
            }
          } catch (Exception ignored) {}

          callback.onError(errorMsg);
        }
      });
    }).exceptionally(e -> {
      Platform.runLater(() -> callback.onError("Lỗi kết nối máy chủ: " + e.getMessage()));
      return null;
    });
  }
}