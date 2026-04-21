package com.auction.controller;

import com.auction.server.models.BidderDTO;
import com.auction.service.BidderService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

public class BidderController {
    private Gson gson;
    private BidderService bidderService;

    public BidderController(){
        this.bidderService=BidderService.getInstance();
        this.gson=new GsonBuilder()
                    .setPrettyPrinting()
                    .excludeFieldsWithoutExposeAnnotation()
                    .create();
    }

    private String createReponse(String status,String message,com.google.gson.JsonElement data){
        JsonObject response = new JsonObject();
        response.addProperty("status", status);
        response.addProperty("message", message);
        if (data != null) {
            response.add("data", data);
        }
        return gson.toJson(response);
    }

    public String handleGetProfile(String jsonRequest){
        try{
            JsonObject req=gson.fromJson(jsonRequest, JsonObject.class);
            int bidderId=req.get("bidder_id").getAsInt();
            BidderDTO profile=bidderService.getBidderProfile(bidderId);
            if (profile!=null)
                return createReponse("success","lấy thông tin thành công",gson.toJsonTree(profile));
            else
                return createReponse("error","không tìm thấy thông tin người đấu giá",null);
        }catch (JsonSyntaxException | NullPointerException e){
            return createReponse("error","yêu cầu không hợp lệ",null);
        }
    }

    public String handleDeposit(String jsonRequest){
        try{
            JsonObject req=gson.fromJson(jsonRequest,JsonObject.class);
            int bidderId=req.get("bidder_id").getAsInt();
            double amount=req.get("amount").getAsDouble();

            String result=bidderService.depositMoney(bidderId, amount);
            if (result.startsWith("Thành công"))
                return createReponse("success","nạp tiền thành công",null);
            else
                return createReponse("error",result,null);
        }catch (Exception e){
            return createReponse("error","yêu cầu không hợp lệ",null);
        }
    }
}
