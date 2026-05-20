package com.auction.service;

import com.auction.controller.UserController;
import com.auction.server.dao.UserDAO;
import com.auction.server.models.UserDTO;
import com.auction.server.models.UserRole;
import com.auction.service.UserService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserControllerTest {

    @Mock
    private UserService userService;

    private UserController userController;
    private final Gson gson = new Gson();

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        userController = new UserController();

        // Inject mock UserService vào controller qua reflection
        Field serviceField = UserController.class.getDeclaredField("userService");
        serviceField.setAccessible(true);
        serviceField.set(userController, userService);
    }

    // =============================================
    // handleLogin()
    // =============================================

    @Test
    void testHandleLogin_Success() {
        Map<String, Object> userDetails = new HashMap<>();
        userDetails.put("userId", 1);
        userDetails.put("role", "BIDDER");
        when(userService.login("user@gmail.com", "pass123")).thenReturn(userDetails);

        JsonObject req = new JsonObject();
        req.addProperty("email", "user@gmail.com");
        req.addProperty("password", "pass123");

        String response = userController.handleLogin(gson.toJson(req));
        JsonObject respObj = gson.fromJson(response, JsonObject.class);

        assertEquals("success", respObj.get("status").getAsString());
        verify(userService).login("user@gmail.com", "pass123");
    }

    @Test
    void testHandleLogin_WrongCredentials() {
        when(userService.login("wrong@gmail.com", "wrong")).thenReturn(null);

        JsonObject req = new JsonObject();
        req.addProperty("email", "wrong@gmail.com");
        req.addProperty("password", "wrong");

        String response = userController.handleLogin(gson.toJson(req));
        JsonObject respObj = gson.fromJson(response, JsonObject.class);

        assertEquals("error", respObj.get("status").getAsString());
    }

    @Test
    void testHandleLogin_MissingEmail() {
        JsonObject req = new JsonObject();
        req.addProperty("password", "pass123");

        String response = userController.handleLogin(gson.toJson(req));
        JsonObject respObj = gson.fromJson(response, JsonObject.class);

        assertEquals("error", respObj.get("status").getAsString());
        verify(userService, never()).login(anyString(), anyString());
    }

    @Test
    void testHandleLogin_MissingPassword() {
        JsonObject req = new JsonObject();
        req.addProperty("email", "user@gmail.com");

        String response = userController.handleLogin(gson.toJson(req));
        JsonObject respObj = gson.fromJson(response, JsonObject.class);

        assertEquals("error", respObj.get("status").getAsString());
        verify(userService, never()).login(anyString(), anyString());
    }

    @Test
    void testHandleLogin_InvalidJson() {
        String response = userController.handleLogin("not-json");
        JsonObject respObj = gson.fromJson(response, JsonObject.class);

        assertEquals("error", respObj.get("status").getAsString());
    }

    @Test
    void testHandleLogin_BannedUser() {
        Map<String, Object> userDetails = new HashMap<>();
        userDetails.put("banned", true);
        when(userService.login("banned@gmail.com", "pass")).thenReturn(userDetails);

        JsonObject req = new JsonObject();
        req.addProperty("email", "banned@gmail.com");
        req.addProperty("password", "pass");

        String response = userController.handleLogin(gson.toJson(req));
        JsonObject respObj = gson.fromJson(response, JsonObject.class);

        assertEquals("error", respObj.get("status").getAsString());
    }

    // =============================================
    // handleRegister()
    // =============================================

    @Test
    void testHandleRegister_Success() {
        when(userService.register("newuser", "pass123", "new@gmail.com", UserRole.BIDDER)).thenReturn(true);

        JsonObject req = new JsonObject();
        req.addProperty("username", "newuser");
        req.addProperty("password", "pass123");
        req.addProperty("email", "new@gmail.com");
        req.addProperty("role", "BIDDER");

        String response = userController.handleRegister(gson.toJson(req));
        JsonObject respObj = gson.fromJson(response, JsonObject.class);

        assertEquals("success", respObj.get("status").getAsString());
        verify(userService).register("newuser", "pass123", "new@gmail.com", UserRole.BIDDER);
    }

    @Test
    void testHandleRegister_DuplicateUser() {
        when(userService.register(anyString(), anyString(), anyString(), any())).thenReturn(false);

        JsonObject req = new JsonObject();
        req.addProperty("username", "existing");
        req.addProperty("password", "pass");
        req.addProperty("email", "exist@gmail.com");

        String response = userController.handleRegister(gson.toJson(req));
        JsonObject respObj = gson.fromJson(response, JsonObject.class);

        assertEquals("error", respObj.get("status").getAsString());
    }

    @Test
    void testHandleRegister_MissingUsername() {
        JsonObject req = new JsonObject();
        req.addProperty("password", "pass");
        req.addProperty("email", "test@gmail.com");

        String response = userController.handleRegister(gson.toJson(req));
        JsonObject respObj = gson.fromJson(response, JsonObject.class);

        assertEquals("error", respObj.get("status").getAsString());
        verify(userService, never()).register(anyString(), anyString(), anyString(), any());
    }

    @Test
    void testHandleRegister_DefaultRoleIsBidder() {
        when(userService.register("user", "pass", "user@gmail.com", UserRole.BIDDER)).thenReturn(true);

        JsonObject req = new JsonObject();
        req.addProperty("username", "user");
        req.addProperty("password", "pass");
        req.addProperty("email", "user@gmail.com");
        // Không gửi role -> phải default là BIDDER

        userController.handleRegister(gson.toJson(req));

        verify(userService).register("user", "pass", "user@gmail.com", UserRole.BIDDER);
    }

    @Test
    void testHandleRegister_InvalidJson() {
        String response = userController.handleRegister("broken-json{{{");
        JsonObject respObj = gson.fromJson(response, JsonObject.class);

        assertEquals("error", respObj.get("status").getAsString());
    }

    // =============================================
    // handleGetProfile()
    // =============================================

    @Test
    void testHandleGetProfile_ValidUser() {
        UserDTO user = new UserDTO();
        user.setId(1);
        user.setUsername("testuser");
        when(userService.getProfile(1)).thenReturn(user);

        String response = userController.handleGetProfile(1);
        JsonObject respObj = gson.fromJson(response, JsonObject.class);

        assertEquals("success", respObj.get("status").getAsString());
        verify(userService).getProfile(1);
    }

    @Test
    void testHandleGetProfile_UserNotFound() {
        when(userService.getProfile(999)).thenReturn(null);

        String response = userController.handleGetProfile(999);
        JsonObject respObj = gson.fromJson(response, JsonObject.class);

        assertEquals("error", respObj.get("status").getAsString());
    }

    // =============================================
    // handleChangePassword()
    // =============================================

    @Test
    void testHandleChangePassword_Success() {
        when(userService.changePassword(1, "oldPass", "newPass")).thenReturn(true);

        JsonObject req = new JsonObject();
        req.addProperty("oldPassword", "oldPass");
        req.addProperty("newPassword", "newPass");

        String response = userController.handleChangePassword(1, gson.toJson(req));
        JsonObject respObj = gson.fromJson(response, JsonObject.class);

        assertEquals("success", respObj.get("status").getAsString());
    }

    @Test
    void testHandleChangePassword_WrongOldPassword() {
        when(userService.changePassword(1, "wrong", "newPass")).thenReturn(false);

        JsonObject req = new JsonObject();
        req.addProperty("oldPassword", "wrong");
        req.addProperty("newPassword", "newPass");

        String response = userController.handleChangePassword(1, gson.toJson(req));
        JsonObject respObj = gson.fromJson(response, JsonObject.class);

        assertEquals("error", respObj.get("status").getAsString());
    }

    @Test
    void testHandleChangePassword_MissingField() {
        JsonObject req = new JsonObject();
        req.addProperty("oldPassword", "oldPass");
        // Thiếu newPassword

        String response = userController.handleChangePassword(1, gson.toJson(req));
        JsonObject respObj = gson.fromJson(response, JsonObject.class);

        assertEquals("error", respObj.get("status").getAsString());
        verify(userService, never()).changePassword(anyInt(), anyString(), anyString());
    }
}