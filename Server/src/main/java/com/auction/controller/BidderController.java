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

    public String handleGetProfile(int secureUserId){
        try{
            BidderDTO profile=bidderService.getBidderProfile(secureUserId);
            if (profile!=null)
                return createReponse("success","Lấy thông tin thành công",gson.toJsonTree(profile));
            else
                return createReponse("error","Không tìm thấy thông tin người đấu giá",null);
        }catch (Exception e){
            return createReponse("error","Lỗi hệ thống",null);
        }
    }

    public String handleDeposit(String jsonRequest, int secureUserId){
        try{
            JsonObject req=gson.fromJson(jsonRequest,JsonObject.class);
            double amount=req.get("amount").getAsDouble();

            String result=bidderService.depositMoney(secureUserId, amount);
            if (result.startsWith("Thành công"))
                return createReponse("success","Nạp tiền thành công",null);
            else
                return createReponse("error",result,null);
        }catch (Exception e){
            return createReponse("error","Yêu cầu không hợp lệ",null);
        }
    }
}
