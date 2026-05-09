package com.auction.client.api;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class ApiService {
  private static ApiService instance;
  private final HttpClient httpClient;

  // ĐẠI LƯU Ý: Sửa lại đường dẫn này cho khớp với Server của Thành/Toản
  // Ví dụ: Nếu Server chạy ở cổng 8080 và mapping là /api/*
  private final String BASE_URL = "http://localhost:8080";

  // Cái ví đựng Cookie (CỰC KỲ QUAN TRỌNG ĐỂ DUY TRÌ ĐĂNG NHẬP)
  private static final CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);

  // Khởi tạo HttpClient với CookieManager
  private ApiService() {
    this.httpClient = HttpClient.newBuilder()
      .cookieHandler(cookieManager) // Gắn ví Cookie vào Shipper
      .build();
  }

  // Singleton Pattern
  public static ApiService getInstance() {
    if (instance == null) {
      instance = new ApiService();
    }
    return instance;
  }

  // =====================================================================
  // 1. HÀM GET (Dùng cho: Lấy danh sách sản phẩm, Xem lịch sử, Xem Profile)
  // =====================================================================
  public CompletableFuture<HttpResponse<String>> sendGetRequest(String endpoint) {
    HttpRequest request = HttpRequest.newBuilder()
      .uri(URI.create(BASE_URL + endpoint))
      .GET()
      .build();
    return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
  }

  // =====================================================================
  // 2. HÀM POST (Dùng cho: Đăng nhập, Đăng ký, Đặt giá Bid, Thêm sản phẩm)
  // =====================================================================
  public CompletableFuture<HttpResponse<String>> sendPostRequest(String endpoint, String jsonBody) {
    HttpRequest request = HttpRequest.newBuilder()
      .uri(URI.create(BASE_URL + endpoint))
      .header("Content-Type", "application/json") // Báo cho Server biết mình gửi JSON
      .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
      .build();
    return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
  }

  // =====================================================================
  // 3. HÀM PUT (Dùng cho: Sửa thông tin cá nhân, Đổi mật khẩu)
  // =====================================================================
  public CompletableFuture<HttpResponse<String>> sendPutRequest(String endpoint, String jsonBody) {
    HttpRequest request = HttpRequest.newBuilder()
      .uri(URI.create(BASE_URL + endpoint))
      .header("Content-Type", "application/json")
      .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
      .build();
    return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
  }

  // =====================================================================
  // 4. HÀM DELETE (Dùng cho: Admin xóa sản phẩm)
  // =====================================================================
  public CompletableFuture<HttpResponse<String>> sendDeleteRequest(String endpoint) {
    HttpRequest request = HttpRequest.newBuilder()
      .uri(URI.create(BASE_URL + endpoint))
      .DELETE()
      .build();
    return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
  }
}