package com.ecommerce.common.event;

import com.ecommerce.common.event.inventory.*;
import com.ecommerce.common.event.notification.*;
import com.ecommerce.common.event.order.*;
import com.ecommerce.common.event.payment.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class DomainEventPojoTest {

    @Test
    @DisplayName("Test 1: OrderCreatedEvent getters and builder")
    void testOrderCreatedEvent() {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId("ORD-101")
                .userId("user-101")
                .productId("PROD-1")
                .quantity(2)
                .totalAmount(new BigDecimal("250.00"))
                .build();

        assertEquals("ORD-101", event.getOrderId());
        assertEquals("user-101", event.getUserId());
        assertEquals("PROD-1", event.getProductId());
        assertEquals(2, event.getQuantity());
        assertEquals(new BigDecimal("250.00"), event.getTotalAmount());
    }

    @Test
    @DisplayName("Test 2: OrderConfirmedEvent getters and builder")
    void testOrderConfirmedEvent() {
        OrderConfirmedEvent event = OrderConfirmedEvent.builder()
                .orderId("ORD-102")
                .userId("user-102")
                .paymentId("PAY-102")
                .build();

        assertEquals("ORD-102", event.getOrderId());
        assertEquals("user-102", event.getUserId());
        assertEquals("PAY-102", event.getPaymentId());
    }

    @Test
    @DisplayName("Test 3: OrderCancelledEvent getters and builder")
    void testOrderCancelledEvent() {
        OrderCancelledEvent event = OrderCancelledEvent.builder()
                .orderId("ORD-103")
                .userId("user-103")
                .productId("PROD-3")
                .quantity(1)
                .reason("Timeout")
                .build();

        assertEquals("ORD-103", event.getOrderId());
        assertEquals("Timeout", event.getReason());
        assertEquals(1, event.getQuantity());
    }

    @Test
    @DisplayName("Test 4: OrderStatus enum coverage")
    void testOrderStatusEnum() {
        assertEquals(6, OrderStatus.values().length);
        assertEquals(OrderStatus.PENDING, OrderStatus.valueOf("PENDING"));
        assertEquals(OrderStatus.INVENTORY_RESERVED, OrderStatus.valueOf("INVENTORY_RESERVED"));
        assertEquals(OrderStatus.CONFIRMED, OrderStatus.valueOf("CONFIRMED"));
        assertEquals(OrderStatus.CANCELLED_OUT_OF_STOCK, OrderStatus.valueOf("CANCELLED_OUT_OF_STOCK"));
        assertEquals(OrderStatus.PAYMENT_FAILED, OrderStatus.valueOf("PAYMENT_FAILED"));
        assertEquals(OrderStatus.CANCELLED, OrderStatus.valueOf("CANCELLED"));
    }

    @Test
    @DisplayName("Test 5: InventoryReservedEvent getters and builder")
    void testInventoryReservedEvent() {
        InventoryReservedEvent event = InventoryReservedEvent.builder()
                .orderId("ORD-201")
                .productId("PROD-A")
                .quantityReserved(3)
                .remainingStock(10)
                .build();

        assertEquals("ORD-201", event.getOrderId());
        assertEquals("PROD-A", event.getProductId());
        assertEquals(3, event.getQuantityReserved());
        assertEquals(10, event.getRemainingStock());
        assertEquals("SUCCESS", event.getStatus());
    }

    @Test
    @DisplayName("Test 6: InventoryReservationFailedEvent getters and builder")
    void testInventoryReservationFailedEvent() {
        InventoryReservationFailedEvent event = InventoryReservationFailedEvent.builder()
                .orderId("ORD-202")
                .productId("PROD-B")
                .requestedQuantity(5)
                .availableStock(2)
                .failureReason("OUT_OF_STOCK")
                .build();

        assertEquals("ORD-202", event.getOrderId());
        assertEquals("PROD-B", event.getProductId());
        assertEquals(5, event.getRequestedQuantity());
        assertEquals(2, event.getAvailableStock());
        assertEquals("OUT_OF_STOCK", event.getFailureReason());
    }

    @Test
    @DisplayName("Test 7: InventoryRestoredEvent getters and builder")
    void testInventoryRestoredEvent() {
        InventoryRestoredEvent event = InventoryRestoredEvent.builder()
                .orderId("ORD-203")
                .productId("PROD-C")
                .quantityRestored(5)
                .updatedStock(25)
                .reason("PAYMENT_FAILURE")
                .build();

        assertEquals("ORD-203", event.getOrderId());
        assertEquals("PROD-C", event.getProductId());
        assertEquals(5, event.getQuantityRestored());
        assertEquals(25, event.getUpdatedStock());
        assertEquals("PAYMENT_FAILURE", event.getReason());
    }

    @Test
    @DisplayName("Test 8: StockUpdatedEvent getters and builder")
    void testStockUpdatedEvent() {
        StockUpdatedEvent event = StockUpdatedEvent.builder()
                .productId("PROD-D")
                .availableStock(42)
                .soldCount(8)
                .isFlashSaleActive(true)
                .build();

        assertEquals("PROD-D", event.getProductId());
        assertEquals(42, event.getAvailableStock());
        assertEquals(8, event.getSoldCount());
        assertTrue(event.getIsFlashSaleActive());
    }

    @Test
    @DisplayName("Test 9: PaymentCompletedEvent getters and builder")
    void testPaymentCompletedEvent() {
        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .orderId("ORD-301")
                .paymentId("PAY-301")
                .userId("user-301")
                .amount(new BigDecimal("1200.00"))
                .transactionReference("TX-MOCK-99")
                .build();

        assertEquals("ORD-301", event.getOrderId());
        assertEquals("PAY-301", event.getPaymentId());
        assertEquals(new BigDecimal("1200.00"), event.getAmount());
        assertEquals("TX-MOCK-99", event.getTransactionReference());
    }

    @Test
    @DisplayName("Test 10: PaymentFailedEvent getters and builder")
    void testPaymentFailedEvent() {
        PaymentFailedEvent event = PaymentFailedEvent.builder()
                .orderId("ORD-302")
                .paymentId("PAY-302")
                .userId("user-302")
                .amount(new BigDecimal("150.00"))
                .failureReason("Declined by bank")
                .build();

        assertEquals("ORD-302", event.getOrderId());
        assertEquals("Declined by bank", event.getFailureReason());
    }

    @Test
    @DisplayName("Test 11: PaymentStatus enum coverage")
    void testPaymentStatusEnum() {
        assertEquals(4, PaymentStatus.values().length);
        assertEquals(PaymentStatus.PENDING, PaymentStatus.valueOf("PENDING"));
        assertEquals(PaymentStatus.SUCCESS, PaymentStatus.valueOf("SUCCESS"));
        assertEquals(PaymentStatus.FAILED, PaymentStatus.valueOf("FAILED"));
        assertEquals(PaymentStatus.REFUNDED, PaymentStatus.valueOf("REFUNDED"));
    }

    @Test
    @DisplayName("Test 12: NotificationEvent getters and builder")
    void testNotificationEvent() {
        NotificationEvent event = NotificationEvent.builder()
                .notificationId("NOTIF-1")
                .userId("user-401")
                .notificationType(NotificationType.ORDER_STATUS_CHANGED)
                .title("Order Confirmed")
                .message("Your order has been placed")
                .targetChannel("/user/queue/notifications")
                .createdAt(Instant.now())
                .build();

        assertEquals("NOTIF-1", event.getNotificationId());
        assertEquals(NotificationType.ORDER_STATUS_CHANGED, event.getNotificationType());
        assertEquals("Order Confirmed", event.getTitle());
    }

    @Test
    @DisplayName("Test 13: NotificationType enum coverage")
    void testNotificationTypeEnum() {
        assertEquals(4, NotificationType.values().length);
        assertNotNull(NotificationType.valueOf("ORDER_STATUS_CHANGED"));
        assertNotNull(NotificationType.valueOf("FLASH_SALE_STOCK_ALERT"));
        assertNotNull(NotificationType.valueOf("PAYMENT_RESULT"));
        assertNotNull(NotificationType.valueOf("SYSTEM_ALERT"));
    }

    @Test
    @DisplayName("Test 14: BaseEvent timestamp and eventId defaults")
    void testBaseEventDefaults() {
        BaseEvent<String> event = BaseEvent.of(EventType.ORDER_CREATED, "corr-1", "order-service", "payload");

        assertNotNull(event.getEventId());
        assertNotNull(event.getTimestamp());
        assertEquals(EventType.ORDER_CREATED, event.getEventType());
        assertEquals("payload", event.getPayload());
        assertEquals("corr-1", event.getCorrelationId());
        assertEquals("order-service", event.getSourceService());
    }

    @Test
    @DisplayName("Test 15: EventType enum values")
    void testEventTypeEnum() {
        assertTrue(EventType.values().length >= 8);
        assertEquals(EventType.ORDER_CREATED, EventType.valueOf("ORDER_CREATED"));
        assertEquals(EventType.INVENTORY_RESERVED, EventType.valueOf("INVENTORY_RESERVED"));
        assertEquals(EventType.PAYMENT_COMPLETED, EventType.valueOf("PAYMENT_COMPLETED"));
    }
}
