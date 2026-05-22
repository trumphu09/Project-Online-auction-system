package com.auction.service;


import com.auction.controller.AuctionController;
import com.auction.service.AuctionService;
import com.auction.service.ItemService;
import com.auction.server.models.ItemDTO;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuctionControllerTest {

    @Mock
    private AuctionService auctionService;

    @Mock
    private ItemService itemService;

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
        when(auctionService.placeBid(1, 2, 200.0))
                .thenReturn("Thành công: Đã chốt giá $200.0");

        JsonObject req = new JsonObject();
        req.addProperty("auction_id", 1);
        req.addProperty("amount", 200.0);

        String response = auctionController.handlePlaceBid(gson.toJson(req), 2);
        JsonObject respObj = gson.fromJson(response, JsonObject.class);

        assertEquals("success", respObj.get("status").getAsString());
        verify(auctionService).placeBid(1, 2, 200.0);
    }

    @Test
    void testHandlePlaceBid_Failure() {
        when(auctionService.placeBid(1, 2, 10.0))
                .thenReturn("Thất bại: Giá không đủ cao, phiên đã đóng, hoặc số dư không đủ!");

        JsonObject req = new JsonObject();
        req.addProperty("auction_id", 1);
        req.addProperty("amount", 10.0);

        String response = auctionController.handlePlaceBid(gson.toJson(req), 2);
        JsonObject respObj = gson.fromJson(response, JsonObject.class);

        assertEquals("error", respObj.get("status").getAsString());
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
        when(auctionService.placeBid(1, 2, 0.0))
                .thenReturn("Lỗi: Số tiền không hợp lệ!");

        JsonObject req = new JsonObject();
        req.addProperty("auction_id", 1);
        req.addProperty("amount", 0.0);

        String response = auctionController.handlePlaceBid(gson.toJson(req), 2);
        JsonObject respObj = gson.fromJson(response, JsonObject.class);

        assertEquals("error", respObj.get("status").getAsString());
    }

    // =============================================
    // handleGetAuctionStatus() / handleGetAuctionData()
    // =============================================

    @Test
    void testHandleGetAuctionData_ValidId() {
        // AuctionService.placeBid() thường kiểm tra qua DAO, test này check controller parse đúng id
        JsonObject req = new JsonObject();
        req.addProperty("auction_id", 5);
        req.addProperty("amount", 100.0);

        when(auctionService.placeBid(5, 1, 100.0)).thenReturn("Thành công: Đã chốt giá $100.0");

        String response = auctionController.handlePlaceBid(gson.toJson(req), 1);

        verify(auctionService).placeBid(5, 1, 100.0);
    }
}