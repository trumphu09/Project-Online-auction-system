package com.auction.service;

import com.auction.server.dao.ItemDAO;
import com.auction.server.models.AuctionManager;
import com.auction.server.models.ItemDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ItemServiceTest {

    @Mock
    private ItemDAO itemDAO;

    @Mock
    private AuctionManager auctionManager;

    private ItemService itemService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        itemService = ItemService.getInstance();

        Field itemDaoField = ItemService.class.getDeclaredField("itemDAO");
        itemDaoField.setAccessible(true);
        itemDaoField.set(itemService, itemDAO);

        // Inject mock AuctionManager singleton
        Field amInstanceField = AuctionManager.class.getDeclaredField("instance");
        amInstanceField.setAccessible(true);
        amInstanceField.set(null, auctionManager);
    }

    // =============================================
    // getAllItems()
    // =============================================

    @Test
    void testGetAllItems_ReturnsCorrectMap() {
        ItemDTO item1 = new ItemDTO();
        item1.setName("Laptop");
        when(itemDAO.getAllItems(1, 10)).thenReturn(Collections.singletonList(item1));
        when(itemDAO.getTotalItemCount()).thenReturn(1);

        Map<String, Object> result = itemService.getAllItems(1, 10);

        assertNotNull(result);
        assertEquals(1, result.get("totalItems"));
        assertEquals(1, result.get("currentPage"));
        List<?> items = (List<?>) result.get("items");
        assertEquals(1, items.size());
        verify(itemDAO).getAllItems(1, 10);
    }

    @Test
    void testGetAllItems_DAOReturnsNull_ReturnsEmptyList() {
        when(itemDAO.getAllItems(1, 10)).thenReturn(null);
        when(itemDAO.getTotalItemCount()).thenReturn(0);

        Map<String, Object> result = itemService.getAllItems(1, 10);

        assertNotNull(result);
        List<?> items = (List<?>) result.get("items");
        assertTrue(items.isEmpty());
    }

    @Test
    void testGetAllItems_TotalPagesCalculation() {
        when(itemDAO.getAllItems(1, 5)).thenReturn(Collections.emptyList());
        when(itemDAO.getTotalItemCount()).thenReturn(11);

        Map<String, Object> result = itemService.getAllItems(1, 5);

        assertEquals(3, result.get("totalPages")); // ceil(11/5) = 3
    }

    // =============================================
    // searchItems()
    // =============================================

    @Test
    void testSearchItems_WithKeyword() {
        ItemDTO item = new ItemDTO();
        item.setName("iPhone 15");
        when(itemDAO.searchItemsByKeyword("iphone")).thenReturn(Collections.singletonList(item));

        List<ItemDTO> result = itemService.searchItems("iphone");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(itemDAO).searchItemsByKeyword("iphone");
    }

    @Test
    void testSearchItems_EmptyKeyword_ReturnsAll() {
        when(itemDAO.getAllItems(1, 50)).thenReturn(Collections.emptyList());

        List<ItemDTO> result = itemService.searchItems("");

        verify(itemDAO).getAllItems(1, 50);
        verify(itemDAO, never()).searchItemsByKeyword(anyString());
    }

    @Test
    void testSearchItems_NullKeyword_ReturnsAll() {
        when(itemDAO.getAllItems(1, 50)).thenReturn(Collections.emptyList());

        List<ItemDTO> result = itemService.searchItems(null);

        verify(itemDAO).getAllItems(1, 50);
    }

    @Test
    void testSearchItems_KeywordWithSpaces_Trimmed() {
        when(itemDAO.searchItemsByKeyword("watch")).thenReturn(Collections.emptyList());

        itemService.searchItems("  watch  ");

        verify(itemDAO).searchItemsByKeyword("watch");
    }

    // =============================================
    // getItemsByCategory()
    // =============================================

    @Test
    void testGetItemsByCategory_ValidCategory() {
        ItemDTO item = new ItemDTO();
        item.setCategory("ELECTRONICS");
        when(itemDAO.getItemsByCategory("ELECTRONICS")).thenReturn(Collections.singletonList(item));

        List<ItemDTO> result = itemService.getItemsByCategory("electronics");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(itemDAO).getItemsByCategory("ELECTRONICS"); // phải uppercase
    }

    @Test
    void testGetItemsByCategory_NullCategory_ReturnsEmpty() {
        List<ItemDTO> result = itemService.getItemsByCategory(null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(itemDAO, never()).getItemsByCategory(anyString());
    }

    @Test
    void testGetItemsByCategory_EmptyCategory_ReturnsEmpty() {
        List<ItemDTO> result = itemService.getItemsByCategory("");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // =============================================
    // addNewItem()
    // =============================================

    @Test
    void testAddNewItem_Success() {
        ItemDTO item = new ItemDTO();
        item.setName("MacBook Pro");
        item.setStartingPrice(1000.0);
        item.setCategory("ELECTRONICS");
        when(itemDAO.addItem(item)).thenReturn(true);

        String result = itemService.addNewItem(item);

        assertTrue(result.startsWith("Thành công"));
        verify(itemDAO).addItem(item);
        verify(auctionManager).broadcastUpdate(eq("NEW_ITEM"), any());
    }

    @Test
    void testAddNewItem_NullItem() {
        String result = itemService.addNewItem(null);

        assertTrue(result.startsWith("Thất bại"));
        verify(itemDAO, never()).addItem(any());
    }

    @Test
    void testAddNewItem_EmptyName() {
        ItemDTO item = new ItemDTO();
        item.setName("");
        item.setStartingPrice(100.0);
        item.setCategory("ART");

        String result = itemService.addNewItem(item);

        assertTrue(result.startsWith("Thất bại"));
        verify(itemDAO, never()).addItem(any());
    }

    @Test
    void testAddNewItem_NullName() {
        ItemDTO item = new ItemDTO();
        item.setName(null);
        item.setStartingPrice(100.0);
        item.setCategory("ART");

        String result = itemService.addNewItem(item);

        assertTrue(result.startsWith("Thất bại"));
    }

    @Test
    void testAddNewItem_InvalidPrice() {
        ItemDTO item = new ItemDTO();
        item.setName("Laptop");
        item.setStartingPrice(0.0);
        item.setCategory("ELECTRONICS");

        String result = itemService.addNewItem(item);

        assertTrue(result.startsWith("Thất bại"));
        verify(itemDAO, never()).addItem(any());
    }

    @Test
    void testAddNewItem_NegativePrice() {
        ItemDTO item = new ItemDTO();
        item.setName("Laptop");
        item.setStartingPrice(-500.0);
        item.setCategory("ELECTRONICS");

        String result = itemService.addNewItem(item);

        assertTrue(result.startsWith("Thất bại"));
    }

    @Test
    void testAddNewItem_MissingCategory() {
        ItemDTO item = new ItemDTO();
        item.setName("Laptop");
        item.setStartingPrice(500.0);
        item.setCategory("");

        String result = itemService.addNewItem(item);

        assertTrue(result.startsWith("Thất bại"));
    }

    @Test
    void testAddNewItem_DAOFails() {
        ItemDTO item = new ItemDTO();
        item.setName("Laptop");
        item.setStartingPrice(500.0);
        item.setCategory("ELECTRONICS");
        when(itemDAO.addItem(item)).thenReturn(false);

        String result = itemService.addNewItem(item);

        assertTrue(result.startsWith("Thất bại"));
        verify(auctionManager, never()).broadcastUpdate(anyString(), any());
    }

    // =============================================
    // deleteItem()
    // =============================================

    @Test
    void testDeleteItem_Success() {
        when(itemDAO.deleteItem(1)).thenReturn(true);

        String result = itemService.deleteItem(1);

        assertTrue(result.startsWith("Thành công"));
        verify(itemDAO).deleteItem(1);
    }

    @Test
    void testDeleteItem_NotFound() {
        when(itemDAO.deleteItem(99)).thenReturn(false);

        String result = itemService.deleteItem(99);

        assertTrue(result.startsWith("Thất bại"));
    }

    @Test
    void testDeleteItem_InvalidId() {
        String result = itemService.deleteItem(0);

        assertTrue(result.startsWith("Thất bại"));
        verify(itemDAO, never()).deleteItem(anyInt());
    }

    @Test
    void testDeleteItem_NegativeId() {
        String result = itemService.deleteItem(-1);

        assertTrue(result.startsWith("Thất bại"));
        verify(itemDAO, never()).deleteItem(anyInt());
    }

    // =============================================
    // getItemDetails()
    // =============================================

    @Test
    void testGetItemDetails_ValidId() {
        ItemDTO item = new ItemDTO();
        item.setId(5);
        item.setName("iPhone");
        when(itemDAO.getItemById(5)).thenReturn(item);

        ItemDTO result = itemService.getItemDetails(5);

        assertNotNull(result);
        assertEquals(5, result.getId());
        verify(itemDAO).getItemById(5);
    }

    @Test
    void testGetItemDetails_InvalidId() {
        ItemDTO result = itemService.getItemDetails(0);

        assertNull(result);
        verify(itemDAO, never()).getItemById(anyInt());
    }

    @Test
    void testGetItemDetails_NotFound() {
        when(itemDAO.getItemById(999)).thenReturn(null);

        ItemDTO result = itemService.getItemDetails(999);

        assertNull(result);
    }

    // =============================================
    // getItemsBySeller()
    // =============================================

    @Test
    void testGetItemsBySeller_ValidSeller() {
        ItemDTO item = new ItemDTO();
        when(itemDAO.getItemsBySellerId(1)).thenReturn(Collections.singletonList(item));

        List<ItemDTO> result = itemService.getItemsBySeller(1);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(itemDAO).getItemsBySellerId(1);
    }
}