package model.dto;

import com.auction.client.model.dto.UserDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test UserDTO phía Client.
 * Không có dependency JavaFX nên chạy được trong CI/headless.
 */
public class UserDTOTest {

    @Test
    void testDefaultValues() {
        UserDTO user = new UserDTO();
        assertEquals(0, user.getId());
        assertNull(user.getUsername());
        assertNull(user.getEmail());
        assertNull(user.getRole());
    }

    @Test
    void testSetAndGetId() {
        UserDTO user = new UserDTO();
        user.setId(5);
        assertEquals(5, user.getId());
    }

    @Test
    void testSetAndGetUsername() {
        UserDTO user = new UserDTO();
        user.setUsername("nguyen_van_a");
        assertEquals("nguyen_van_a", user.getUsername());
    }

    @Test
    void testSetAndGetEmail() {
        UserDTO user = new UserDTO();
        user.setEmail("test@gmail.com");
        assertEquals("test@gmail.com", user.getEmail());
    }

    @Test
    void testSetAndGetRole() {
        UserDTO user = new UserDTO();
        user.setRole("BIDDER");
        assertEquals("BIDDER", user.getRole());
    }



    @Test
    void testSetAndGetSaleCount() {
        UserDTO user = new UserDTO();
        user.setSaleCount(10);
        assertEquals(10, user.getSaleCount());
    }

    @Test
    void testIsSellerRole() {
        UserDTO user = new UserDTO();
        user.setRole("SELLER");
        assertEquals("SELLER", user.getRole());
        assertNotEquals("BIDDER", user.getRole());
    }
}
