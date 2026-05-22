package com.auction.service;


import com.auction.controller.AuctionController;
import com.auction.service.AuctionService;
import com.auction.server.dao.BidsDAO.BidResult;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuctionControllerTest {

    @Mock
    private AuctionService auctionService;

    private AuctionController auctionController;
    private final Gson gson = new Gson();

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        auctionController = new AuctionController();

        // Inject mock services vào controller
        Field auctionServiceField = AuctionController.class.getDeclaredField("auctionService");
        auctionServiceField.setAccessible(true);
        auctionServiceField.set(auctionController, auctionService);
    }

    // =============================================
    // handlePlaceBid()
    // =============================================

    @Test
    void testHandlePlaceBid_Success() {
        BidResult bidResult = new BidResult(true, false, null, "Thành công: Đã chốt giá $200.0");
        when(auctionService.placeBid(1, 2, 200.0))
                .thenReturn(bidResult);

        JsonObject req = new JsonObject();
        req.addProperty("auction_id", 1);
        req.addProperty("amount", 200.0);

        String response = auctionController.handlePlaceBid(gson.toJson(req), 2);
        JsonObject respObj = gson.fromJson(response, JsonObject.class);

        assertEquals("success", respObj.get("status").getAsString());
        assertEquals("Thành công: Đã chốt giá $200.0", respObj.get("message").getAsString());
        verify(auctionService).placeBid(1, 2, 200.0);
    }

    @Test
    void testHandlePlaceBid_Failure() {
        BidResult bidResult = new BidResult(false, false, null, "Thất bại: Giá không đủ cao, phiên đã đóng, hoặc số dư không đủ!");
        when(auctionService.placeBid(1, 2, 10.0))
                .thenReturn(bidResult);

        JsonObject req = new JsonObject();
        req.addProperty("auction_id", 1);
        req.addProperty("amount", 10.0);

        String response = auctionController.handlePlaceBid(gson.toJson(req), 2);
        JsonObject respObj = gson.fromJson(response, JsonObject.class);

        assertEquals("error", respObj.get("status").getAsString());
        assertEquals("Thất bại: Giá không đủ cao, phiên đã đóng, hoặc số dư không đủ!", respObj.get("message").getAsString());
    }

    @Test
    void testHandlePlaceBid_MissingAuctionId() {
        JsonObject req = new JsonObject();
        req.addProperty("amount", 200.0);
        // Thiếu auction_id

        String response = auctionController.handlePlaceBid(gson.toJson(req), 2);
        JsonObject respObj = gson.fromJson(response, JsonObject.class);

        assertEquals("error", respObj.get("status").getAsString());
        verify(auctionService, never()).placeBid(anyInt(), anyInt(), anyDouble());
    }

    @Test
    void testHandlePlaceBid_MissingAmount() {
        JsonObject req = new JsonObject();
        req.addProperty("auction_id", 1);
        // Thiếu amount

        String response = auctionController.handlePlaceBid(gson.toJson(req), 2);
        JsonObject respObj = gson.fromJson(response, JsonObject.class);

        assertEquals("error", respObj.get("status").getAsString());
        verify(auctionService, never()).placeBid(anyInt(), anyInt(), anyDouble());
    }

    @Test
    void testHandlePlaceBid_InvalidJson() {
        String response = auctionController.handlePlaceBid("not-valid-json", 2);
        JsonObject respObj = gson.fromJson(response, JsonObject.class);

        assertEquals("error", respObj.get("status").getAsString());
    }

    @Test
    void testHandlePlaceBid_ZeroAmount() {
        BidResult bidResult = new BidResult(false, false, null, "Lỗi: Số tiền không hợp lệ!");
        when(auctionService.placeBid(1, 2, 0.0))
                .thenReturn(bidResult);

        JsonObject req = new JsonObject();
        req.addProperty("auction_id", 1);
        req.addProperty("amount", 0.0);

        String response = auctionController.handlePlaceBid(gson.toJson(req), 2);
        JsonObject respObj = gson.fromJson(response, JsonObject.class);

        assertEquals("error", respObj.get("status").getAsString());
    }

    // =============================================
    // handlePlaceBid() với time extension
    // =============================================

    @Test
    void testHandlePlaceBid_WithTimeExtension() {
        LocalDateTime newEndTime = LocalDateTime.now().plusMinutes(5);
        BidResult bidResult = new BidResult(true, true, newEndTime, "Thành công: Đã chốt giá $150.0 - Thời gian gia hạn");
        when(auctionService.placeBid(3, 5, 150.0))
                .thenReturn(bidResult);

        JsonObject req = new JsonObject();
        req.addProperty("auction_id", 3);
        req.addProperty("amount", 150.0);

        String response = auctionController.handlePlaceBid(gson.toJson(req), 5);
        JsonObject respObj = gson.fromJson(response, JsonObject.class);

        assertEquals("success", respObj.get("status").getAsString());
        assertTrue(respObj.getAsJsonObject("data").get("time_extended").getAsBoolean());
        assertEquals(3, respObj.getAsJsonObject("data").get("auction_id").getAsInt());
        assertEquals(150.0, respObj.getAsJsonObject("data").get("amount").getAsDouble());
    }

    @Test
    void testHandlePlaceBid_NegativeAmount() {
        BidResult bidResult = new BidResult(false, false, null, "Lỗi: Số tiền không hợp lệ!");
        when(auctionService.placeBid(1, 2, -50.0))
                .thenReturn(bidResult);

        JsonObject req = new JsonObject();
        req.addProperty("auction_id", 1);
        req.addProperty("amount", -50.0);

        String response = auctionController.handlePlaceBid(gson.toJson(req), 2);
        JsonObject respObj = gson.fromJson(response, JsonObject.class);

        assertEquals("error", respObj.get("status").getAsString());
    }

    @Test
    void testHandlePlaceBid_NullRequest() {
        String response = auctionController.handlePlaceBid(null, 2);
        JsonObject respObj = gson.fromJson(response, JsonObject.class);

        assertEquals("error", respObj.get("status").getAsString());
    }
}