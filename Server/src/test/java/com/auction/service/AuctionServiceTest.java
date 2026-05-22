package com.auction.service;

import com.auction.server.dao.BidsDAO;
import com.auction.server.dao.BidsDAO.BidResult;
import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.UserDAO;
import com.auction.server.models.AuctionManager;
import com.auction.server.models.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuctionServiceTest {

    @Mock
    private BidsDAO bidsDAO;
    @Mock
    private AuctionDAO auctionDAO;
    @Mock
    private UserDAO userDAO;
    @Mock
    private AuctionManager auctionManager;

    private AuctionService auctionService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        auctionService = AuctionService.getInstance();

        // Inject mocks into AuctionService
        Field bidsDaoField = AuctionService.class.getDeclaredField("bidsDAO");
        bidsDaoField.setAccessible(true);
        bidsDaoField.set(auctionService, bidsDAO);

        Field auctionDaoField = AuctionService.class.getDeclaredField("auctionDAO");
        auctionDaoField.setAccessible(true);
        auctionDaoField.set(auctionService, auctionDAO);

        Field userDaoField = AuctionService.class.getDeclaredField("userDAO");
        userDaoField.setAccessible(true);
        userDaoField.set(auctionService, userDAO);

        // Inject the mock auctionManager into the AuctionManager's static instance field
        Field amInstanceField = AuctionManager.class.getDeclaredField("instance");
        amInstanceField.setAccessible(true);
        amInstanceField.set(null, auctionManager); // Set static field
    }

    @Test
    void testPlaceBidWithValidAmount() {
        // Arrange
        int auctionId = 1;
        int bidderId = 1;
        double amount = 100.0;
        UserDTO bidder = new UserDTO();
        bidder.setUsername("bidder");

        BidResult bidResult = new BidResult(true, false, null, "Thành công: Đã chốt giá $100.0");
        when(bidsDAO.executeBidDetailed(auctionId, bidderId, amount)).thenReturn(bidResult);
        when(userDAO.getUserById(bidderId)).thenReturn(bidder);

        // Act
        BidResult result = auctionService.placeBid(auctionId, bidderId, amount);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("Thành công: Đã chốt giá $100.0", result.getMessage());
        verify(bidsDAO).executeBidDetailed(auctionId, bidderId, amount);
        verify(auctionManager).broadcastUpdate(eq("NEW_BID"), any(Map.class));
    }

    @Test
    void testPlaceBidWithInvalidAmount() {
        // Act
        BidResult result = auctionService.placeBid(1, 1, 0);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("Lỗi: Số tiền không hợp lệ!", result.getMessage());
        verify(bidsDAO, never()).executeBidDetailed(anyInt(), anyInt(), anyDouble());
    }

    @Test
    void testPlaceBidWhenBidFails() {
        // Arrange
        int auctionId = 1;
        int bidderId = 1;
        double amount = 100.0;
        UserDTO bidder = new UserDTO();
        bidder.setUsername("bidder");

        BidResult bidResult = new BidResult(false, false, null, "Thất bại: Giá không đủ cao, phiên đã đóng, hoặc số dư không đủ!");
        when(bidsDAO.executeBidDetailed(auctionId, bidderId, amount)).thenReturn(bidResult);
        when(userDAO.getUserById(bidderId)).thenReturn(bidder);

        // Act
        BidResult result = auctionService.placeBid(auctionId, bidderId, amount);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("Thất bại: Giá không đủ cao, phiên đã đóng, hoặc số dư không đủ!", result.getMessage());
        verify(bidsDAO).executeBidDetailed(auctionId, bidderId, amount);
        verify(auctionManager, never()).broadcastUpdate(anyString(), any(Map.class));
    }

    @Test
    void testPlaceBidWithTimeExtension() {
        // Arrange
        int auctionId = 1;
        int bidderId = 1;
        double amount = 150.0;
        LocalDateTime newEndTime = LocalDateTime.now().plusMinutes(1);
        UserDTO bidder = new UserDTO();
        bidder.setUsername("bidder");

        BidResult bidResult = new BidResult(true, true, newEndTime, "Thành công: Đã chốt giá $150.0 - Thời gian gia hạn");
        when(bidsDAO.executeBidDetailed(auctionId, bidderId, amount)).thenReturn(bidResult);
        when(userDAO.getUserById(bidderId)).thenReturn(bidder);

        // Act
        BidResult result = auctionService.placeBid(auctionId, bidderId, amount);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertTrue(result.isTimeExtended());
        assertNotNull(result.getNewEndTime());
        verify(bidsDAO).executeBidDetailed(auctionId, bidderId, amount);
        verify(auctionManager, times(2)).broadcastUpdate(anyString(), any(Map.class));
    }

    @Test
    void testPlaceBidWithNegativeAmount() {
        // Act
        BidResult result = auctionService.placeBid(1, 1, -50.0);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("Lỗi: Số tiền không hợp lệ!", result.getMessage());
        verify(bidsDAO, never()).executeBidDetailed(anyInt(), anyInt(), anyDouble());
    }
}