package com.ecommerce.payment.domain.entity;

import com.ecommerce.common.event.payment.PaymentStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class PaymentTransactionTest {

    @Test
    void testCreatePaymentTransaction() {
        PaymentTransaction txn = PaymentTransaction.create("order_123", "user_123", new BigDecimal("500.00"), "MOMO");

        assertNotNull(txn.getPaymentId());
        assertFalse(txn.getPaymentId().trim().isEmpty()); // Kill mutant: replaced return value with ""
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

    @Test
    void testCreateWithNullUserIdAndMethod() {
        PaymentTransaction txn = PaymentTransaction.create("order_999", null, new BigDecimal("10.00"), null);

        assertEquals("unknown-user", txn.getUserId());
        assertEquals("VNPAY", txn.getPaymentMethod());
        assertEquals(PaymentStatus.PENDING, txn.getStatus());
    }

    @Test
    void testIsAlreadyPaidWhenFailed() {
        PaymentTransaction txn = PaymentTransaction.builder()
                .status(PaymentStatus.FAILED)
                .build();
        assertFalse(txn.isAlreadyPaid());
    }

    @Test
    void testBuilderAllFields() {
        Instant now = Instant.now();
        PaymentTransaction txn = PaymentTransaction.builder()
                .id(99L)
                .paymentId("PAY-99")
                .orderId("ORD-99")
                .userId("USER-99")
                .amount(new BigDecimal("99.99"))
                .paymentMethod("CREDIT_CARD")
                .status(PaymentStatus.PENDING)
                .transactionRef("REF-99")
                .failureReason("None")
                .paidAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertEquals(99L, txn.getId());
        assertEquals("PAY-99", txn.getPaymentId());
        assertEquals("ORD-99", txn.getOrderId());
        assertEquals("USER-99", txn.getUserId());
        assertEquals(new BigDecimal("99.99"), txn.getAmount());
        assertEquals("CREDIT_CARD", txn.getPaymentMethod());
        assertEquals(PaymentStatus.PENDING, txn.getStatus());
        assertEquals("REF-99", txn.getTransactionRef());
        assertEquals("None", txn.getFailureReason());
        assertEquals(now, txn.getPaidAt());
        assertEquals(now, txn.getCreatedAt());
        assertEquals(now, txn.getUpdatedAt());
    }

    @Test
    void testMultipleFailuresUpdateReason() {
        PaymentTransaction txn = PaymentTransaction.create("order_1", "user_1", BigDecimal.TEN, "MOCK");
        txn.markAsFailed("Initial error");
        assertEquals("Initial error", txn.getFailureReason());

        txn.markAsFailed("Subsequent error");
        assertEquals("Subsequent error", txn.getFailureReason());
    }

    @Test
    void testMarkAsSuccessUpdatesTimestamp() {
        PaymentTransaction txn = PaymentTransaction.create("order_2", "user_2", BigDecimal.TEN, "MOCK");
        Instant before = Instant.now().minusSeconds(1);

        txn.markAsSuccess("TXN-REF");

        assertTrue(txn.getPaidAt().isAfter(before));
        assertTrue(txn.getUpdatedAt().isAfter(before));
    }

    @Test
    void testProcessUpdatesAmountAndTimestamp() {
        PaymentTransaction txn = PaymentTransaction.create("order_3", "user_3", BigDecimal.ONE, "MOCK");
        Instant before = Instant.now().minusSeconds(1);

        txn.process(new BigDecimal("49.99"));

        assertEquals(new BigDecimal("49.99"), txn.getAmount());
        assertTrue(txn.getUpdatedAt().isAfter(before));
    }
}
