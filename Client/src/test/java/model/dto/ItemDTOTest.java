package model.dto;

import com.auction.client.model.dto.ItemDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test các DTO model phía Client.
 * Không phụ thuộc JavaFX nên chạy được trong CI/headless.
 */
public class ItemDTOTest {

    @Test
    void testDefaultValues() {
        ItemDTO item = new ItemDTO();
        assertEquals(0, item.getId());
        assertNull(item.getName());
        assertNull(item.getDescription());
        assertEquals(0.0, item.getStartingPrice());
        assertNull(item.getCategory());
    }

    @Test
    void testSetAndGetId() {
        ItemDTO item = new ItemDTO();
        item.setId(10);
        assertEquals(10, item.getId());
    }

    @Test
    void testSetAndGetName() {
        ItemDTO item = new ItemDTO();
        item.setName("iPhone 15 Pro");
        assertEquals("iPhone 15 Pro", item.getName());
    }

    @Test
    void testSetAndGetDescription() {
        ItemDTO item = new ItemDTO();
        item.setDescription("Mô tả sản phẩm");
        assertEquals("Mô tả sản phẩm", item.getDescription());
    }

    @Test
    void testSetAndGetStartingPrice() {
        ItemDTO item = new ItemDTO();
        item.setStartingPrice(25_000_000.0);
        assertEquals(25_000_000.0, item.getStartingPrice());
    }

    @Test
    void testSetAndGetCategory() {
        ItemDTO item = new ItemDTO();
        item.setCategory("ELECTRONICS");
        assertEquals("ELECTRONICS", item.getCategory());
    }

    @Test
    void testSetAndGetAuctionId() {
        ItemDTO item = new ItemDTO();
        item.setAuctionId(42);
        assertEquals(42, item.getAuctionId());
    }

    @Test
    void testSetAndGetCurrentMaxPrice() {
        ItemDTO item = new ItemDTO();
        item.setCurrentMaxPrice(30_000_000.0);
        assertEquals(30_000_000.0, item.getCurrentMaxPrice());
    }

    @Test
    void testSetAndGetPriceStep() {
        ItemDTO item = new ItemDTO();
        item.setPriceStep(500_000.0);
        assertEquals(500_000.0, item.getPriceStep());
    }

    @Test
    void testSetAndGetSellerId() {
        ItemDTO item = new ItemDTO();
        item.setSellerId(7);
        assertEquals(7, item.getSellerId());
    }

    @Test
    void testSetAndGetStatus() {
        ItemDTO item = new ItemDTO();
        item.setStatus("RUNNING");
        assertEquals("RUNNING", item.getStatus());
    }

    @Test
    void testSetAndGetImagePath() {
        ItemDTO item = new ItemDTO();
        item.setImagePath("/uploads/img.jpg");
        assertEquals("/uploads/img.jpg", item.getImagePath());
    }

    @Test
    void testSetAndGetStartTime() {
        ItemDTO item = new ItemDTO();
        item.setStartTime("2025-01-01T08:00:00");
        assertEquals("2025-01-01T08:00:00", item.getStartTime());
    }

    @Test
    void testSetAndGetEndTime() {
        ItemDTO item = new ItemDTO();
        item.setEndTime("2025-01-07T20:00:00");
        assertEquals("2025-01-07T20:00:00", item.getEndTime());
    }

    @Test
    void testHighestBidderId() {
        ItemDTO item = new ItemDTO();
        item.setHighestBidderId(99);
        assertEquals(99, item.getHighestBidderId());
    }

    @Test
    void testAuctionIdZeroByDefault() {
        ItemDTO item = new ItemDTO();
        assertEquals(0, item.getAuctionId());
    }

    @Test
    void testCurrentMaxPriceZeroByDefault() {
        ItemDTO item = new ItemDTO();
        assertEquals(0.0, item.getCurrentMaxPrice());
    }

    @Test
    void testMultipleFieldsAtOnce() {
        ItemDTO item = new ItemDTO();
        item.setId(10);
        item.setName("MacBook Air");
        item.setCategory("ELECTRONICS");
        item.setStartingPrice(15_000_000.0);
        item.setCurrentMaxPrice(18_000_000.0);
        item.setPriceStep(200_000.0);
        item.setStatus("OPEN");
        item.setSellerId(3);
        item.setAuctionId(7);

        assertEquals(10, item.getId());
        assertEquals("MacBook Air", item.getName());
        assertEquals("ELECTRONICS", item.getCategory());
        assertEquals(15_000_000.0, item.getStartingPrice());
        assertEquals(18_000_000.0, item.getCurrentMaxPrice());
        assertEquals(200_000.0, item.getPriceStep());
        assertEquals("OPEN", item.getStatus());
        assertEquals(3, item.getSellerId());
        assertEquals(7, item.getAuctionId());
    }

    @Test
    void testOverwriteExistingValues() {
        ItemDTO item = new ItemDTO();
        item.setName("Tên cũ");
        item.setName("Tên mới");
        assertEquals("Tên mới", item.getName());
    }

    @Test
    void testCategoryAllCategories() {
        ItemDTO e = new ItemDTO(); e.setCategory("ELECTRONICS");
        ItemDTO v = new ItemDTO(); v.setCategory("VEHICLE");
        ItemDTO a = new ItemDTO(); a.setCategory("ART");

        assertEquals("ELECTRONICS", e.getCategory());
        assertEquals("VEHICLE", v.getCategory());
        assertEquals("ART", a.getCategory());
    }
}