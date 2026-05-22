package com.auction.service;

import com.auction.server.dao.PaymentDAO;
import com.auction.server.models.Payment;
import com.auction.server.models.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PaymentServiceTest {

    @Mock
    private PaymentDAO paymentDAO;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        paymentService = PaymentService.getInstance();

        // Inject mock qua reflection
        Field paymentDaoField = PaymentService.class.getDeclaredField("paymentDAO");
        paymentDaoField.setAccessible(true);
        paymentDaoField.set(paymentService, paymentDAO);
    }

    // =============================================
    // createInvoice()
    // =============================================

    @Test
    void testCreateInvoice_Success() {
        when(paymentDAO.createPaymentInvoice(1, 2, 3, 500.0)).thenReturn(true);

        String result = paymentService.createInvoice(1, 2, 3, 500.0);

        assertTrue(result.startsWith("Thành công"));
        verify(paymentDAO).createPaymentInvoice(1, 2, 3, 500.0);
    }

    @Test
    void testCreateInvoice_DAOFails() {
        when(paymentDAO.createPaymentInvoice(1, 2, 3, 500.0)).thenReturn(false);

        String result = paymentService.createInvoice(1, 2, 3, 500.0);

        assertTrue(result.startsWith("Thất bại"));
        verify(paymentDAO).createPaymentInvoice(1, 2, 3, 500.0);
    }

    @Test
    void testCreateInvoice_InvalidAuctionId() {
        String result = paymentService.createInvoice(0, 2, 3, 500.0);

        assertTrue(result.startsWith("Thất bại"));
        verify(paymentDAO, never()).createPaymentInvoice(anyInt(), anyInt(), anyInt(), anyDouble());
    }

    @Test
    void testCreateInvoice_InvalidBidderId() {
        String result = paymentService.createInvoice(1, 0, 3, 500.0);

        assertTrue(result.startsWith("Thất bại"));
        verify(paymentDAO, never()).createPaymentInvoice(anyInt(), anyInt(), anyInt(), anyDouble());
    }

    @Test
    void testCreateInvoice_InvalidSellerId() {
        String result = paymentService.createInvoice(1, 2, 0, 500.0);

        assertTrue(result.startsWith("Thất bại"));
        verify(paymentDAO, never()).createPaymentInvoice(anyInt(), anyInt(), anyInt(), anyDouble());
    }

    @Test
    void testCreateInvoice_BidderSameAsSeller() {
        String result = paymentService.createInvoice(1, 5, 5, 500.0);

        assertTrue(result.startsWith("Thất bại"));
        verify(paymentDAO, never()).createPaymentInvoice(anyInt(), anyInt(), anyInt(), anyDouble());
    }

    @Test
    void testCreateInvoice_AmountZero() {
        String result = paymentService.createInvoice(1, 2, 3, 0.0);

        assertTrue(result.startsWith("Thất bại"));
        verify(paymentDAO, never()).createPaymentInvoice(anyInt(), anyInt(), anyInt(), anyDouble());
    }

    @Test
    void testCreateInvoice_NegativeAmount() {
        String result = paymentService.createInvoice(1, 2, 3, -100.0);

        assertTrue(result.startsWith("Thất bại"));
        verify(paymentDAO, never()).createPaymentInvoice(anyInt(), anyInt(), anyInt(), anyDouble());
    }

    // =============================================
    // executePayment()
    // =============================================

    @Test
    void testExecutePayment_Success() {
        when(paymentDAO.processPayment(1)).thenReturn(true);

        String result = paymentService.executePayment(1);

        assertTrue(result.startsWith("Thành công"));
        verify(paymentDAO).processPayment(1);
    }

    @Test
    void testExecutePayment_Failure() {
        when(paymentDAO.processPayment(1)).thenReturn(false);

        String result = paymentService.executePayment(1);

        assertTrue(result.startsWith("Thất bại"));
        verify(paymentDAO).processPayment(1);
    }

    @Test
    void testExecutePayment_InvalidId() {
        String result = paymentService.executePayment(0);

        assertTrue(result.startsWith("Thất bại"));
        verify(paymentDAO, never()).processPayment(anyInt());
    }

    @Test
    void testExecutePayment_NegativeId() {
        String result = paymentService.executePayment(-5);

        assertTrue(result.startsWith("Thất bại"));
        verify(paymentDAO, never()).processPayment(anyInt());
    }

    // =============================================
    // getUserPaymentHistory()
    // =============================================

    @Test
    void testGetUserPaymentHistory_ValidUser() {
        Payment p = new Payment(1, 10, 2, 3, 500.0, PaymentStatus.COMPLETED, LocalDateTime.now());
        when(paymentDAO.getPaymentHistoryByUser(2)).thenReturn(Collections.singletonList(p));

        List<Payment> result = paymentService.getUserPaymentHistory(2);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(paymentDAO).getPaymentHistoryByUser(2);
    }

    @Test
    void testGetUserPaymentHistory_InvalidUserId() {
        List<Payment> result = paymentService.getUserPaymentHistory(0);

        assertNull(result);
        verify(paymentDAO, never()).getPaymentHistoryByUser(anyInt());
    }

    @Test
    void testGetUserPaymentHistory_NegativeUserId() {
        List<Payment> result = paymentService.getUserPaymentHistory(-1);

        assertNull(result);
        verify(paymentDAO, never()).getPaymentHistoryByUser(anyInt());
    }

    @Test
    void testGetUserPaymentHistory_EmptyList() {
        when(paymentDAO.getPaymentHistoryByUser(99)).thenReturn(Collections.emptyList());

        List<Payment> result = paymentService.getUserPaymentHistory(99);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(paymentDAO).getPaymentHistoryByUser(99);
    }
}