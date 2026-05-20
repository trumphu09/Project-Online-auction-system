package com.auction.service;

import com.auction.controller.WatchlistController;
import com.auction.server.dao.WatchlistDAO;
import com.auction.server.models.ItemDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class WatchlistControllerTest {

    @Mock
    private WatchlistDAO watchlistDAO;

    private WatchlistController watchlistController;
    private MockedStatic<WatchlistDAO> mockedWatchlistDAO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        mockedWatchlistDAO = Mockito.mockStatic(WatchlistDAO.class);
        mockedWatchlistDAO.when(WatchlistDAO::getInstance).thenReturn(watchlistDAO);

        watchlistController = WatchlistController.getInstance();
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        mockedWatchlistDAO.close();
    }

    // =============================================
    // handleAddToWatchlist()
    // =============================================

    @Test
    void testHandleAddToWatchlist_Success() {
        when(watchlistDAO.addToWatchlist(1, 10)).thenReturn(true);

        Map<String, Object> result = watchlistController.handleAddToWatchlist(1, 10);

        assertEquals("success", result.get("status"));
        verify(watchlistDAO).addToWatchlist(1, 10);
    }

    @Test
    void testHandleAddToWatchlist_AlreadyExists() {
        when(watchlistDAO.addToWatchlist(1, 10)).thenReturn(false);

        Map<String, Object> result = watchlistController.handleAddToWatchlist(1, 10);

        assertEquals("error", result.get("status"));
        verify(watchlistDAO).addToWatchlist(1, 10);
    }

    // =============================================
    // handleGetWatchlist()
    // =============================================

    @Test
    void testHandleGetWatchlist_WithItems() {
        ItemDTO item1 = new ItemDTO();
        item1.setName("Laptop");
        ItemDTO item2 = new ItemDTO();
        item2.setName("iPhone");
        when(watchlistDAO.getWatchlistByUserId(1)).thenReturn(Arrays.asList(item1, item2));

        Map<String, Object> result = watchlistController.handleGetWatchlist(1);

        assertEquals("success", result.get("status"));
        List<?> items = (List<?>) result.get("data");
        assertEquals(2, items.size());
        verify(watchlistDAO).getWatchlistByUserId(1);
    }

    @Test
    void testHandleGetWatchlist_EmptyWatchlist() {
        when(watchlistDAO.getWatchlistByUserId(2)).thenReturn(Collections.emptyList());

        Map<String, Object> result = watchlistController.handleGetWatchlist(2);

        assertEquals("success", result.get("status"));
        List<?> items = (List<?>) result.get("data");
        assertTrue(items.isEmpty());
    }
}