package com.ecommerce.payment.domain.entity;

import com.ecommerce.common.event.payment.PaymentStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PaymentTransactionTest {

    @Test
    void testCreatePaymentTransaction() {
        PaymentTransaction txn = PaymentTransaction.create("order_123", "user_123", new BigDecimal("500.00"), "MOMO");

        assertNotNull(txn.getPaymentId());
        assertEquals("order_123", txn.getOrderId());
        assertEquals("user_123", txn.getUserId());
        assertEquals(new BigDecimal("500.00"), txn.getAmount());
        assertEquals("MOMO", txn.getPaymentMethod());
        assertEquals(PaymentStatus.PENDING, txn.getStatus());
        assertNotNull(txn.getCreatedAt());
        assertNotNull(txn.getUpdatedAt());
    }

    @Test
    void testIsAlreadyPaid() {
        PaymentTransaction txn = PaymentTransaction.builder()
                .status(PaymentStatus.SUCCESS)
                .build();
        assertTrue(txn.isAlreadyPaid());

        PaymentTransaction txnPending = PaymentTransaction.builder()
                .status(PaymentStatus.PENDING)
                .build();
        assertFalse(txnPending.isAlreadyPaid());
    }

    @Test
    void testProcessSuccess() {
        PaymentTransaction txn = PaymentTransaction.builder()
                .status(PaymentStatus.PENDING)
                .build();

        txn.process(new BigDecimal("200.00"));
        assertEquals(new BigDecimal("200.00"), txn.getAmount());
        assertEquals(PaymentStatus.PENDING, txn.getStatus());
    }

    @Test
    void testProcessAlreadyPaid() {
        PaymentTransaction txn = PaymentTransaction.builder()
                .status(PaymentStatus.SUCCESS)
                .build();

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                txn.process(new BigDecimal("200.00")));
        assertEquals("Cannot process an already paid transaction.", exception.getMessage());
    }

    @Test
    void testMarkAsFailed() {
        PaymentTransaction txn = PaymentTransaction.builder()
                .status(PaymentStatus.PENDING)
                .build();

        txn.markAsFailed("Insufficient funds");
        assertEquals(PaymentStatus.FAILED, txn.getStatus());
        assertEquals("Insufficient funds", txn.getFailureReason());
    }

    @Test
    void testMarkAsSuccess() {
        PaymentTransaction txn = PaymentTransaction.builder()
                .status(PaymentStatus.PENDING)
                .build();

        txn.markAsSuccess("TXN-123456");
        assertEquals(PaymentStatus.SUCCESS, txn.getStatus());
        assertEquals("TXN-123456", txn.getTransactionRef());
        assertNotNull(txn.getPaidAt());
    }
}
