package com.auction.controller;

import com.auction.server.dao.BidsDAO.BidResult;
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

            BidResult result = auctionService.placeBid(auctionId, secureUserId, amount);

            JsonObject data = new JsonObject();
            data.addProperty("auction_id", auctionId);
            data.addProperty("amount", amount);
            data.addProperty("time_extended", result.isTimeExtended());
            if (result.getNewEndTime() != null) {
                data.addProperty("new_end_time", result.getNewEndTime().toString());
            }

            if (result.isSuccess()) {
                return createResponse("success", result.getMessage(), data);
            }
            return createResponse("error", result.getMessage(), null);

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
        if (data != null) response.add("data", data);
        return gson.toJson(response);
    }
}