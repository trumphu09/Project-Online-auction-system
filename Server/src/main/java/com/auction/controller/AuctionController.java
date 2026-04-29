package com.auction.controller;

import com.auction.service.AuctionService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

public class AuctionController {
    private final Gson gson;
    private final AuctionService auctionService;

    public AuctionController() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.auctionService = AuctionService.getInstance();
    }

    public String handlePlaceBid(String jsonRequest, int secureUserId) {
        try {
            JsonObject req = gson.fromJson(jsonRequest, JsonObject.class);
            
            if (req == null || !req.has("auction_id") || !req.has("amount")) {
                return createResponse("error", "Thất bại: Thiếu 'auction_id' hoặc 'amount'!", null);
            }

            int auctionId = req.get("auction_id").getAsInt();
            double amount = req.get("amount").getAsDouble();

            // Sử dụng userId an toàn từ session, không dùng bidder_id từ JSON
            String result = auctionService.placeBid(auctionId, secureUserId, amount);

            if (result.startsWith("Thành công")) {
                return createResponse("success", result, null);
            } else {
                return createResponse("error", result, null);
            }

        } catch (JsonSyntaxException | NullPointerException e) {
            return createResponse("error", "Thất bại: Sai định dạng JSON!", null);
        } catch (Exception e) {
            return createResponse("error", "Lỗi hệ thống: " + e.getMessage(), null);
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