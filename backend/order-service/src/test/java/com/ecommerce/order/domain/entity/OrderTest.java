package com.ecommerce.order.domain.entity;

import com.ecommerce.common.event.order.OrderStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    @Test
    void testCreateOrder() {
        Order order = Order.createNew("user_123", "user@example.com", "PROD_1", "Product Title", 2, new BigDecimal("100.00"));

        assertNotNull(order.getOrderId());
        assertEquals("user_123", order.getUserId());
        assertEquals("user@example.com", order.getUserEmail());
        assertEquals(new BigDecimal("200.00"), order.getTotalAmount()); // 2 * 100
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertNotNull(order.getCreatedAt());
        assertNotNull(order.getUpdatedAt());
    }

    @Test
    void testCreateOrderWithInvalidQuantity() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                Order.createNew("user_123", "user@example.com", "PROD_1", "Title", 0, new BigDecimal("100.00")));
        assertEquals("Quantity must be greater than zero", exception.getMessage());
    }

    @Test
    void testMarkInventoryReservationFailed() {
        Order order = Order.createNew("user_123", "user@example.com", "PROD_1", "Title", 1, new BigDecimal("100.00"));
        order.markInventoryReservationFailed("Inventory unavailable");

        assertEquals(OrderStatus.CANCELLED_OUT_OF_STOCK, order.getStatus());
        assertEquals("Inventory unavailable", order.getCancelReason());
    }

    @Test
    void testMarkPaymentCompleted() {
        Order order = Order.createNew("user_123", "user@example.com", "PROD_1", "Title", 1, new BigDecimal("100.00"));
        order.markPaymentCompleted("PAY_123");

        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        assertEquals("PAY_123", order.getPaymentId());
    }
}
