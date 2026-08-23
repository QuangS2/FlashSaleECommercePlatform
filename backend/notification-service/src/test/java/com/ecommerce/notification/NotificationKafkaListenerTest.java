package com.ecommerce.notification;

import com.ecommerce.common.event.EventType;
import com.ecommerce.common.event.inventory.InventoryReservedEvent;
import com.ecommerce.common.event.order.OrderCancelledEvent;
import com.ecommerce.common.event.order.OrderConfirmedEvent;
import com.ecommerce.common.event.payment.PaymentFailedEvent;
import com.ecommerce.notification.dto.NotificationMessage;
import com.ecommerce.notification.listener.NotificationKafkaListener;
import com.ecommerce.notification.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class NotificationKafkaListenerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private Acknowledgment acknowledgment;

    private ObjectMapper objectMapper;
    private NotificationKafkaListener kafkaListener;

    @BeforeEach
    public void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        kafkaListener = new NotificationKafkaListener(notificationService, objectMapper);
    }

    @Test
    @DisplayName("Test 1: onEvent - ORDER_CONFIRMED triggers user notification and Manual ACK")
    public void testOnEvent_OrderConfirmed() throws Exception {
        OrderConfirmedEvent payload = OrderConfirmedEvent.builder()
                .orderId("ORD-SUCCESS-001")
                .userId("user_1001")
                .confirmedAt(Instant.now())
                .build();

        Map<String, Object> baseEvent = Map.of(
                "eventId", "EVT-100",
                "eventType", EventType.ORDER_CONFIRMED.name(),
                "payload", payload
        );
        String json = objectMapper.writeValueAsString(baseEvent);

        kafkaListener.onEvent(json, acknowledgment);

        verify(notificationService).sendNotificationToUser(eq("user_1001"), any(NotificationMessage.class));
        verify(notificationService).broadcastOrderUpdate(eq("ORD-SUCCESS-001"), any(NotificationMessage.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Test 2: onEvent - INVENTORY_RESERVED triggers live stock broadcast and Manual ACK")
    public void testOnEvent_InventoryReserved() throws Exception {
        InventoryReservedEvent payload = InventoryReservedEvent.builder()
                .orderId("ORD-SUCCESS-002")
                .productId("PROD-FLASH-01")
                .quantityReserved(1)
                .remainingStock(99)
                .reservedAt(Instant.now())
                .build();

        Map<String, Object> baseEvent = Map.of(
                "eventId", "EVT-101",
                "eventType", EventType.INVENTORY_RESERVED.name(),
                "payload", payload
        );
        String json = objectMapper.writeValueAsString(baseEvent);

        kafkaListener.onEvent(json, acknowledgment);

        verify(notificationService).broadcastStockUpdate(eq("PROD-FLASH-01"), eq(99), eq("RESERVED"));
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Test 3: onEvent - PAYMENT_FAILED triggers user notification and Manual ACK")
    public void testOnEvent_PaymentFailed() throws Exception {
        PaymentFailedEvent payload = PaymentFailedEvent.builder()
                .paymentId("PAY-FAIL-999")
                .orderId("ORD-FAIL-003")
                .userId("user_1002")
                .amount(new BigDecimal("15000000"))
                .failureReason("Thẻ hết hạn hoặc không đủ số dư")
                .failedAt(Instant.now())
                .build();

        Map<String, Object> baseEvent = Map.of(
                "eventId", "EVT-102",
                "eventType", EventType.PAYMENT_FAILED.name(),
                "payload", payload
        );
        String json = objectMapper.writeValueAsString(baseEvent);

        kafkaListener.onEvent(json, acknowledgment);

        verify(notificationService).sendNotificationToUser(eq("user_1002"), any(NotificationMessage.class));
        verify(notificationService).broadcastOrderUpdate(eq("ORD-FAIL-003"), any(NotificationMessage.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Test 4: onEvent - ORDER_CANCELLED triggers user notification and Manual ACK")
    public void testOnEvent_OrderCancelled() throws Exception {
        OrderCancelledEvent payload = OrderCancelledEvent.builder()
                .orderId("ORD-CANCEL-004")
                .userId("user_1003")
                .reason("Kho Flash Sale đã hết hàng")
                .cancelledAt(Instant.now())
                .build();

        Map<String, Object> baseEvent = Map.of(
                "eventId", "EVT-103",
                "eventType", EventType.ORDER_CANCELLED.name(),
                "payload", payload
        );
        String json = objectMapper.writeValueAsString(baseEvent);

        kafkaListener.onEvent(json, acknowledgment);

        verify(notificationService).sendNotificationToUser(eq("user_1003"), any(NotificationMessage.class));
        verify(notificationService).broadcastOrderUpdate(eq("ORD-CANCEL-004"), any(NotificationMessage.class));
        verify(acknowledgment).acknowledge();
    }
}
