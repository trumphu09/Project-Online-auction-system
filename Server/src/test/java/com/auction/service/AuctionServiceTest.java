package com.auction.service;

import com.auction.server.dao.BidsDAO;
import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.UserDAO;
import com.auction.server.models.AuctionManager;
import com.auction.server.models.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
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

        when(userDAO.getUserById(bidderId)).thenReturn(bidder);
        when(bidsDAO.executeBid(auctionId, bidderId, amount)).thenReturn(true);

        // Act
        String result = auctionService.placeBid(auctionId, bidderId, amount);

        // Assert
        assertEquals("Thành công: Đã chốt giá $100.0", result);
        verify(bidsDAO).executeBid(auctionId, bidderId, amount);
        // Now this verification will work because AuctionManager.getInstance() returns our mock
        verify(auctionManager).broadcastUpdate(eq("NEW_BID"), any(Map.class));
    }

    @Test
    void testPlaceBidWithInvalidAmount() {
        // Act
        String result = auctionService.placeBid(1, 1, 0);

        // Assert
        assertEquals("Lỗi: Số tiền không hợp lệ!", result);
        verify(bidsDAO, never()).executeBid(anyInt(), anyInt(), anyDouble());
    }

    @Test
    void testPlaceBidWhenBidFails() {
        // Arrange
        int auctionId = 1;
        int bidderId = 1;
        double amount = 100.0;
        UserDTO bidder = new UserDTO();
        bidder.setUsername("bidder");

        when(userDAO.getUserById(bidderId)).thenReturn(bidder);
        when(bidsDAO.executeBid(auctionId, bidderId, amount)).thenReturn(false);

        // Act
        String result = auctionService.placeBid(auctionId, bidderId, amount);

        // Assert
        assertEquals("Thất bại: Giá không đủ cao, phiên đã đóng, hoặc số dư không đủ!", result);
        verify(bidsDAO).executeBid(auctionId, bidderId, amount);
        verify(auctionManager, never()).broadcastUpdate(anyString(), any(Map.class));
    }
}