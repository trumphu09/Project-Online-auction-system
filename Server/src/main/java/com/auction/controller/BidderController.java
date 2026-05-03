package com.auction.controller;

import com.auction.service.BidderService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

public class BidderController {
    private final Gson gson;
    private final BidderService bidderService;

    public BidderController(){
        this.bidderService = BidderService.getInstance();
        this.gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .excludeFieldsWithoutExposeAnnotation()
                    .create();
    }

    private String createResponse(String status, String message, com.google.gson.JsonElement data){
        JsonObject response = new JsonObject();
        response.addProperty("status", status);
        response.addProperty("message", message);
        if (data != null) {
            response.add("data", data);
        }
        return gson.toJson(response);
    }

    public String handleDeposit(String jsonRequest, int secureUserId){
        try{
            JsonObject req = gson.fromJson(jsonRequest, JsonObject.class);
            if (req == null || !req.has("amount")) {
                return createResponse("error", "Thiếu trường 'amount' trong yêu cầu.", null);
            }
            double amount = req.get("amount").getAsDouble();

            String result = bidderService.depositMoney(secureUserId, amount);
            if (result.startsWith("Thành công"))
                return createResponse("success", result, null);
            else
                return createResponse("error", result, null);
        } catch (JsonSyntaxException | NullPointerException e){
            return createResponse("error", "Yêu cầu JSON không hợp lệ.", null);
        } catch (Exception e){
            e.printStackTrace();
            return createResponse("error", "Lỗi hệ thống.", null);
        }
    }
}
