package com.auction.controller;
import com.auction.server.models.Payment;
import com.auction.service.PaymentService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.util.List;
public class PaymentController {
    private final Gson gson;
    private PaymentService paymentService;

    public PaymentController(){
        this.gson=new GsonBuilder().setPrettyPrinting().create();
        this.paymentService=PaymentService.getInstance();
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

    // ==========================================
    // 1. API: TẠO HÓA ĐƠN (Dành cho Admin hoặc Hệ thống tự gọi)
    // JSON: {"auction_id": 5, "bidder_id": 108, "seller_id": 105, "amount": 2000.0}
    // ==========================================
    public String handleCreateInvoice(String jsonRequest){
        try{
            JsonObject req=gson.fromJson(jsonRequest,JsonObject.class);
            if (!req.has("auction_id") || !req.has("bidder_id") || !req.has("seller_id") || !req.has("amount")) {
                return createResponse("error", "Thiếu thông tin bắt buộc để tạo hóa đơn!", null);
            }

            int auctionId = req.get("auction_id").getAsInt();
            int bidderId = req.get("bidder_id").getAsInt();
            int sellerId = req.get("seller_id").getAsInt();
            double amount = req.get("amount").getAsDouble();

            String result = paymentService.createInvoice(auctionId, bidderId, sellerId, amount);

            if (result.startsWith("Thành công")) {
                return createResponse("success", result, null);
            } else {
                return createResponse("error", result, null);
            }
        } catch (JsonSyntaxException | NullPointerException e) {
            return createResponse("error", "Sai định dạng JSON!", null);
        } catch (Exception e) {
            return createResponse("error", "Lỗi server: " + e.getMessage(), null);
        }
    }

    // ==========================================
    // 2. API: THỰC HIỆN THANH TOÁN CHỐT ĐƠN
    // ==========================================
    public String handleExecutePayment(String jsonRequest) {
        try {
            // Dùng com.google.gson.JsonObject để đồng bộ với thư viện bạn đang dùng
            com.google.gson.JsonObject req = gson.fromJson(jsonRequest, com.google.gson.JsonObject.class);

            // Kiểm tra auction_id thay vì payment_id
            if (!req.has("auction_id")) {
                return createResponse("error", "Thiếu mã phiên đấu giá (auction_id)!", null);
            }

            int auctionId = req.get("auction_id").getAsInt();

            // Khởi tạo trực tiếp DAO để xử lý nghiệp vụ thanh toán đã viết
            com.auction.server.dao.PaymentDAO paymentDAO = new com.auction.server.dao.PaymentDAO();
            boolean isSuccess = paymentDAO.processPayment(auctionId);

            if (isSuccess) {
                return createResponse("success", "Thanh toán thành công!", null);
            } else {
                return createResponse("error", "Lỗi xử lý thanh toán (Phiên chưa kết thúc hoặc lỗi DB)!", null);
            }
        } catch (com.google.gson.JsonSyntaxException | NullPointerException e) {
            return createResponse("error", "Sai định dạng JSON!", null);
        } catch (Exception e) {
            return createResponse("error", "Lỗi server: " + e.getMessage(), null);
        }
    }

    // ==========================================
    // 3. API: XEM LỊCH SỬ GIAO DỊCH CỦA TÀI KHOẢN
    // (Vì là lệnh GET nên thường truyền ID thẳng thay vì chuỗi JSON)
    // ==========================================
    public String handleGetPaymentHistory(int userId) {
        try {
            List<Payment> history = paymentService.getUserPaymentHistory(userId);

            if (history == null || history.isEmpty()) {
                return createResponse("success", "Tài khoản này chưa có giao dịch nào.", null);
            }

            // Dùng Gson biến List Object thành mảng JSON cực mượt
            return createResponse("success", "Lấy lịch sử giao dịch thành công.", gson.toJsonTree(history));

        } catch (Exception e) {
            return createResponse("error", "Lỗi server khi lấy lịch sử: " + e.getMessage(), null);
        }
    }
}
