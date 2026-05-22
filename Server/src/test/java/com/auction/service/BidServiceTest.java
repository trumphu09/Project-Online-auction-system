package com.auction.service;

import com.auction.server.AuctionServer;
import com.auction.server.dao.BidsDAO;
import com.auction.server.dao.DatabaseConnection;
import com.auction.server.dao.ItemDAO;
import com.auction.server.models.AuctionStatus;
import com.auction.server.models.BidTransactionDTO;
import com.auction.server.models.Item;
import com.auction.server.models.ItemDTO; // Import ItemDTO
import com.auction.server.websocket.AuctionWebSocketServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BidServiceTest {

    // --- Mocks for all external dependencies ---
    @Mock private BidsDAO bidsDAO;
    @Mock private ItemDAO itemDAO;
    @Mock private ItemDTO mockedItem; // CHANGED: Mock ItemDTO instead of Item
    @Mock private AuctionWebSocketServer webSocketServer;
    @Mock private DatabaseConnection databaseConnection;
    @Mock private Connection mockJdbcConnection;
    @Mock private PreparedStatement mockPreparedStatement;

    // --- Test subject ---
    private BidService bidService;

    // --- Mockito helpers ---
    private AutoCloseable openMocks;
    private MockedStatic<AuctionServer> mockedAuctionServer;
    private MockedStatic<DatabaseConnection> mockedDbConnection;

    @BeforeEach
    void setUp() throws Exception {
        openMocks = MockitoAnnotations.openMocks(this);

        // Step 1: Mock all static Singleton getters BEFORE any code uses them.
        mockedAuctionServer = Mockito.mockStatic(AuctionServer.class);
        mockedAuctionServer.when(AuctionServer::getWebSocketServer).thenReturn(webSocketServer);

        mockedDbConnection = Mockito.mockStatic(DatabaseConnection.class);
        mockedDbConnection.when(DatabaseConnection::getInstance).thenReturn(databaseConnection);
        when(databaseConnection.getConnection()).thenReturn(mockJdbcConnection);
        
        // Teach mockJdbcConnection to return a mock PreparedStatement
        when(mockJdbcConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        // Teach mockPreparedStatement to do nothing for set methods and return 1 for executeUpdate
        doNothing().when(mockPreparedStatement).setInt(anyInt(), anyInt());
        doNothing().when(mockPreparedStatement).setDouble(anyInt(), anyDouble());
        doNothing().when(mockPreparedStatement).setString(anyInt(), anyString());
        doNothing().when(mockPreparedStatement).setObject(anyInt(), any());
        when(mockPreparedStatement.executeUpdate()).thenReturn(1); // Assume 1 row affected for successful operations

        // Step 2: Now it's safe to get the service instance.
        bidService = BidService.getInstance();

        // Step 3: Replace the service's internal DAOs with our @Mock DAOs.
        Field bidsDaoField = BidService.class.getDeclaredField("bidsDAO");
        bidsDaoField.setAccessible(true);
        bidsDaoField.set(bidService, bidsDAO);

        Field itemDaoField = BidService.class.getDeclaredField("itemDAO");
        itemDaoField.setAccessible(true);
        itemDaoField.set(bidService, itemDAO);
    }

    @AfterEach
    void tearDown() throws Exception {
        // Clean up static mocks and regular mocks to prevent test pollution
        mockedAuctionServer.close();
        mockedDbConnection.close();
        openMocks.close();
    }

    @Test
    void testGetBidHistoryWithValidItemId() {
        // Arrange
        List<BidTransactionDTO> history = Collections.singletonList(new BidTransactionDTO(1, 1, "testuser", 100.0, LocalDateTime.now()));
        when(bidsDAO.getBidHistoryByItemId(1)).thenReturn(history);

        // Act
        List<BidTransactionDTO> result = bidService.getBidHistory(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bidsDAO).getBidHistoryByItemId(1);
    }

    @Test
    void testProcessBidSuccess() {
        // Arrange
        int itemId = 1;
        int userId = 1;
        double bidAmount = 100.0;
        double newMaxPriceForBroadcast = bidAmount; 

        when(bidsDAO.executeBid(itemId, userId, bidAmount)).thenReturn(true);
        when(itemDAO.extendAuctionTimeIfNeeded(itemId)).thenReturn(true);
        when(itemDAO.getItemById(itemId)).thenReturn(mockedItem);

        // --- Teach the mocked item how to behave for all relevant getters ---
        when(mockedItem.getId()).thenReturn(itemId);
        when(mockedItem.getCurrentMaxPrice()).thenReturn(newMaxPriceForBroadcast); 
        when(mockedItem.getSellerId()).thenReturn(2);
        when(mockedItem.getHighestBidderId()).thenReturn(userId);
        // ItemDTO uses String for status, not AuctionStatus enum
        when(mockedItem.getStatus()).thenReturn(AuctionStatus.RUNNING.name()); 
        when(mockedItem.getStartingPrice()).thenReturn(50.0);
        when(mockedItem.getName()).thenReturn("Test Item");
        when(mockedItem.getDescription()).thenReturn("A test item");
        when(mockedItem.getCategory()).thenReturn("Electronics");
        when(mockedItem.getStartTime()).thenReturn(LocalDateTime.now().minusDays(1).toString()); // ItemDTO uses String for time
        when(mockedItem.getEndTime()).thenReturn(LocalDateTime.now().plusDays(1).toString()); // ItemDTO uses String for time

        // Act
        boolean result = bidService.processBid(itemId, userId, bidAmount);

        // Assert
        assertTrue(result);
        verify(bidsDAO).executeBid(itemId, userId, bidAmount);
        verify(itemDAO).extendAuctionTimeIfNeeded(itemId);
        verify(webSocketServer).broadcastPriceUpdate(itemId, newMaxPriceForBroadcast);
    }

    @Test
    void testProcessBidFailure() {
        // Arrange
        int itemId = 1;
        int userId = 1;
        double bidAmount = 100.0;
        when(bidsDAO.executeBid(itemId, userId, bidAmount)).thenReturn(false);

        // Act
        boolean result = bidService.processBid(itemId, userId, bidAmount);

        // Assert
        assertFalse(result);
        verify(bidsDAO).executeBid(itemId, userId, bidAmount);
        verify(itemDAO, never()).extendAuctionTimeIfNeeded(anyInt());
        verify(webSocketServer, never()).broadcastPriceUpdate(anyInt(), anyDouble());
    }
}