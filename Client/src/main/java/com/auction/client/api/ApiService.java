package com.auction.client.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class ApiService {
    private static ApiService instance;
    private final HttpClient httpClient;
    private final String BASE_URL = "http://localhost:8080/api"; // Sửa IP/Port server tại đây

    private ApiService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    public static ApiService getInstance() {
        if (instance == null) {
            instance = new ApiService();
        }
        return instance;
    }

    // Hàm gửi Request tổng quát
    public CompletableFuture<String> sendPostRequest(String endpoint, String json) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }
}