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

    public String handlePlaceBid(String jsonRequest) {
        try {
            // 1. Bóc hộp JSON từ Client
            JsonObject req = gson.fromJson(jsonRequest, JsonObject.class);
            
            // Kiểm tra xem Client có gửi đủ 3 thông tin này không
            if (!req.has("auction_id") || !req.has("bidder_id") || !req.has("amount")) {
                return createResponse("error", "Thất bại: Thiếu thông tin bắt buộc!", null);
            }

            int auctionId = req.get("auction_id").getAsInt();
            int bidderId = req.get("bidder_id").getAsInt();
            double amount = req.get("amount").getAsDouble();

            // 2. Giao cho Trọng tài xử lý (Trọng tài sẽ gọi BidsDAO khóa dòng, trừ tiền...)
            String result = auctionService.placeBid(auctionId, bidderId, amount);

            // 3. Đóng gói kết quả trả về
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
