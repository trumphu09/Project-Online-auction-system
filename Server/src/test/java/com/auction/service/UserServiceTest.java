package com.auction.service;

import com.auction.server.dao.UserDAO;
import com.auction.server.models.UserDTO;
import com.auction.server.models.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    @Mock
    private UserDAO userDAO;

    private UserService userService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        userService = UserService.getInstance();

        // --- Inject mock using reflection ---
        Field userDaoField = UserService.class.getDeclaredField("userDAO");
        userDaoField.setAccessible(true);
        userDaoField.set(userService, userDAO);
    }

    @Test
    void testLoginWithValidCredentials() {
        // Arrange
        String email = "test@example.com";
        String password = "password";
        Map<String, Object> expected = new HashMap<>();
        expected.put("userId", 1);
        when(userDAO.loginUser(email, password)).thenReturn(expected);

        // Act
        Map<String, Object> result = userService.login(email, password);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.get("userId"));
        verify(userDAO).loginUser(email, password);
    }

    @Test
    void testLoginWithInvalidCredentials() {
        // Arrange
        when(userDAO.loginUser("wrong@email.com", "wrong")).thenReturn(null);

        // Act
        Map<String, Object> result = userService.login("wrong@email.com", "wrong");

        // Assert
        assertNull(result);
        verify(userDAO).loginUser("wrong@email.com", "wrong");
    }

    @Test
    void testRegisterWithValidData() {
        // Arrange
        when(userDAO.registerUser("username", "password", "email@example.com", UserRole.BIDDER)).thenReturn(true);

        // Act
        boolean result = userService.register("username", "password", "email@example.com", UserRole.BIDDER);

        // Assert
        assertTrue(result);
        verify(userDAO).registerUser("username", "password", "email@example.com", UserRole.BIDDER);
    }

    @Test
    void testRegisterWithNullUsername() {
        // Act
        boolean result = userService.register(null, "password", "email@example.com", UserRole.BIDDER);

        // Assert
        assertFalse(result);
        verify(userDAO, never()).registerUser(any(), any(), any(), any());
    }

    @Test
    void testGetProfileWithValidUserId() {
        // Arrange
        UserDTO user = new UserDTO();
        user.setId(1);
        when(userDAO.getUserById(1)).thenReturn(user);

        // Act
        UserDTO result = userService.getProfile(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        verify(userDAO).getUserById(1);
    }

    @Test
    void testGetProfileWithInvalidUserId() {
        // Act
        UserDTO result = userService.getProfile(0);

        // Assert
        assertNull(result);
        verify(userDAO, never()).getUserById(anyInt());
    }
}