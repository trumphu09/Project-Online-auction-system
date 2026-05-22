package validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test riêng phần validation logic của RegisterController.
 * Tách logic ra để test không cần khởi động JavaFX.
 *
 * Để dùng: tạo class RegisterValidator (hoặc test trực tiếp logic inline).
 * Ở đây mình test các business rule tương ứng với code trong RegisterController:
 *   - Email phải kết thúc bằng "@gmail.com"
 *   - Username và password không được rỗng
 */
public class RegisterValidatorTest {

    // ---- Helper replicate logic từ RegisterController ----
    private boolean isValidEmail(String email) {
        return email != null && email.endsWith("@gmail.com");
    }

    private boolean isValidInput(String username, String password) {
        return username != null && !username.trim().isEmpty()
                && password != null && !password.trim().isEmpty();
    }

    // =============================================
    // Email validation
    // =============================================

    @Test
    void testValidGmailEmail() {
        assertTrue(isValidEmail("nguyen@gmail.com"));
    }

    @Test
    void testInvalidEmail_NotGmail() {
        assertFalse(isValidEmail("nguyen@yahoo.com"));
    }

    @Test
    void testInvalidEmail_NoAt() {
        assertFalse(isValidEmail("nguyengmail.com"));
    }

    @Test
    void testInvalidEmail_Empty() {
        assertFalse(isValidEmail(""));
    }

    @Test
    void testInvalidEmail_Null() {
        assertFalse(isValidEmail(null));
    }

    @Test
    void testValidEmail_WithDots() {
        assertTrue(isValidEmail("nguyen.van.a@gmail.com"));
    }

    @Test
    void testValidEmail_WithNumbers() {
        assertTrue(isValidEmail("user123@gmail.com"));
    }

    // =============================================
    // Input validation (username + password)
    // =============================================

    @Test
    void testValidUsernameAndPassword() {
        assertTrue(isValidInput("user123", "pass456"));
    }

    @Test
    void testEmptyUsername() {
        assertFalse(isValidInput("", "pass456"));
    }

    @Test
    void testBlankUsername() {
        assertFalse(isValidInput("   ", "pass456"));
    }

    @Test
    void testNullUsername() {
        assertFalse(isValidInput(null, "pass456"));
    }

    @Test
    void testEmptyPassword() {
        assertFalse(isValidInput("user123", ""));
    }

    @Test
    void testBlankPassword() {
        assertFalse(isValidInput("user123", "   "));
    }

    @Test
    void testNullPassword() {
        assertFalse(isValidInput("user123", null));
    }

    @Test
    void testBothNull() {
        assertFalse(isValidInput(null, null));
    }

    @Test
    void testBothEmpty() {
        assertFalse(isValidInput("", ""));
    }
}