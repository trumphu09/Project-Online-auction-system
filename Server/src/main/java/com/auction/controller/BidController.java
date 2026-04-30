package com.auction.controller;

import com.auction.service.BidService;
import com.auction.server.models.BidTransactionDTO;
import com.auction.server.models.Item;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.auction.server.utils.LocalDateTimeAdapter;

import java.time.LocalDateTime;
import java.util.List;

public class BidController {

    private final BidService bidService = BidService.getInstance();
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    public String handleGetBidHistory(int itemId) {
        try {
            List<BidTransactionDTO> history = bidService.getBidHistory(itemId);
            return createResponse("success", "Lấy lịch sử đấu giá thành công.", gson.toJsonTree(history));
        } catch (Exception e) {
            e.printStackTrace();
            return createResponse("error", "Lỗi hệ thống khi lấy lịch sử đấu giá.", null);
        }
    }

    public String handleGetActiveBids(int userId) {
        try {
            List<Item> items = bidService.getActiveBids(userId);
            return createResponse("success", "Lấy danh sách các phiên đang tham gia thành công.", gson.toJsonTree(items));
        } catch (Exception e) {
            e.printStackTrace();
            return createResponse("error", "Lỗi hệ thống khi lấy các phiên đang tham gia.", null);
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