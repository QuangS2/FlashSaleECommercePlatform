package com.ecommerce.common.event;

import com.ecommerce.common.event.inventory.InventoryReservationFailedEvent;
import com.ecommerce.common.event.inventory.InventoryReservedEvent;
import com.ecommerce.common.event.inventory.InventoryRestoredEvent;
import com.ecommerce.common.event.inventory.StockUpdatedEvent;
import com.ecommerce.common.event.notification.NotificationEvent;
import com.ecommerce.common.event.notification.NotificationType;
import com.ecommerce.common.event.order.OrderCancelledEvent;
import com.ecommerce.common.event.order.OrderConfirmedEvent;
import com.ecommerce.common.event.order.OrderCreatedEvent;
import com.ecommerce.common.event.order.OrderStatus;
import com.ecommerce.common.event.payment.PaymentCompletedEvent;
import com.ecommerce.common.event.payment.PaymentFailedEvent;
import com.ecommerce.common.event.payment.PaymentStatus;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class EventSerializationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("Test 1: BaseEvent Envelope Generic Serialization & Deserialization")
    public void testBaseEventEnvelope() throws Exception {
        OrderCreatedEvent payload = OrderCreatedEvent.builder()
                .orderId("ORD-" + UUID.randomUUID())
                .userId("user_1001")
                .userEmail("quang.dev@ecommerce.vn")
                .productId("PROD-IPHONE-15-FLASH")
                .productTitle("iPhone 15 Pro Max Flash Sale")
                .quantity(1)
                .unitPrice(new BigDecimal("29990000"))
                .totalAmount(new BigDecimal("29990000"))
                .status(OrderStatus.PENDING)
                .createdAt(Instant.now())
                .build();

        BaseEvent<OrderCreatedEvent> event = BaseEvent.of(
                EventType.ORDER_CREATED,
                "CORR-ORD-12345",
                "order-service",
                payload
        );

        String json = objectMapper.writeValueAsString(event);
        System.out.println("[TEST LOG] Serialized BaseEvent JSON:\n" + json);

        assertThat(json).contains("\"eventType\":\"ORDER_CREATED\"");
        assertThat(json).contains("\"sourceService\":\"order-service\"");
        assertThat(json).contains("\"correlationId\":\"CORR-ORD-12345\"");
        assertThat(json).contains("\"productId\":\"PROD-IPHONE-15-FLASH\"");

        BaseEvent<OrderCreatedEvent> deserialized = objectMapper.readValue(
                json,
                new TypeReference<BaseEvent<OrderCreatedEvent>>() {}
        );

        assertThat(deserialized).isNotNull();
        assertThat(deserialized.getEventId()).isEqualTo(event.getEventId());
        assertThat(deserialized.getEventType()).isEqualTo(EventType.ORDER_CREATED);
        assertThat(deserialized.getPayload().getOrderId()).isEqualTo(payload.getOrderId());
        assertThat(deserialized.getPayload().getTotalAmount()).isEqualByComparingTo(new BigDecimal("29990000"));
    }

    @Test
    @DisplayName("Test 2: Order Domain Events Serialization Round-Trip")
    public void testOrderDomainEvents() throws Exception {
        // 1. OrderCreatedEvent
        OrderCreatedEvent created = OrderCreatedEvent.builder()
                .orderId("ORD-001")
                .userId("usr-1")
                .productId("PROD-001")
                .quantity(2)
                .unitPrice(new BigDecimal("500000"))
                .totalAmount(new BigDecimal("1000000"))
                .status(OrderStatus.PENDING)
                .build();
        String createdJson = objectMapper.writeValueAsString(created);
        OrderCreatedEvent createdObj = objectMapper.readValue(createdJson, OrderCreatedEvent.class);
        assertThat(createdObj.getOrderId()).isEqualTo("ORD-001");
        assertThat(createdObj.getStatus()).isEqualTo(OrderStatus.PENDING);

        // 2. OrderConfirmedEvent
        OrderConfirmedEvent confirmed = OrderConfirmedEvent.builder()
                .orderId("ORD-001")
                .userId("usr-1")
                .productId("PROD-001")
                .quantity(2)
                .totalAmount(new BigDecimal("1000000"))
                .paymentId("PAY-999")
                .status(OrderStatus.CONFIRMED)
                .build();
        String confirmedJson = objectMapper.writeValueAsString(confirmed);
        OrderConfirmedEvent confirmedObj = objectMapper.readValue(confirmedJson, OrderConfirmedEvent.class);
        assertThat(confirmedObj.getPaymentId()).isEqualTo("PAY-999");
        assertThat(confirmedObj.getStatus()).isEqualTo(OrderStatus.CONFIRMED);

        // 3. OrderCancelledEvent
        OrderCancelledEvent cancelled = OrderCancelledEvent.builder()
                .orderId("ORD-001")
                .userId("usr-1")
                .productId("PROD-001")
                .quantity(2)
                .reason("Flash Sale Stock Exhausted")
                .status(OrderStatus.CANCELLED)
                .build();
        String cancelledJson = objectMapper.writeValueAsString(cancelled);
        OrderCancelledEvent cancelledObj = objectMapper.readValue(cancelledJson, OrderCancelledEvent.class);
        assertThat(cancelledObj.getReason()).isEqualTo("Flash Sale Stock Exhausted");
        assertThat(cancelledObj.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("Test 3: Inventory Domain Events Serialization Round-Trip")
    public void testInventoryDomainEvents() throws Exception {
        // 1. InventoryReservedEvent
        InventoryReservedEvent reserved = InventoryReservedEvent.builder()
                .orderId("ORD-001")
                .productId("PROD-001")
                .quantityReserved(1)
                .remainingStock(99)
                .status("SUCCESS")
                .build();
        String reservedJson = objectMapper.writeValueAsString(reserved);
        InventoryReservedEvent reservedObj = objectMapper.readValue(reservedJson, InventoryReservedEvent.class);
        assertThat(reservedObj.getRemainingStock()).isEqualTo(99);

        // 2. InventoryReservationFailedEvent
        InventoryReservationFailedEvent failed = InventoryReservationFailedEvent.builder()
                .orderId("ORD-002")
                .productId("PROD-001")
                .requestedQuantity(5)
                .availableStock(0)
                .failureReason("OUT_OF_STOCK")
                .build();
        String failedJson = objectMapper.writeValueAsString(failed);
        InventoryReservationFailedEvent failedObj = objectMapper.readValue(failedJson, InventoryReservationFailedEvent.class);
        assertThat(failedObj.getFailureReason()).isEqualTo("OUT_OF_STOCK");

        // 3. InventoryRestoredEvent (Compensating)
        InventoryRestoredEvent restored = InventoryRestoredEvent.builder()
                .orderId("ORD-003")
                .productId("PROD-001")
                .quantityRestored(1)
                .updatedStock(100)
                .reason("PAYMENT_FAILED_REFUND_STOCK")
                .build();
        String restoredJson = objectMapper.writeValueAsString(restored);
        InventoryRestoredEvent restoredObj = objectMapper.readValue(restoredJson, InventoryRestoredEvent.class);
        assertThat(restoredObj.getUpdatedStock()).isEqualTo(100);

        // 4. StockUpdatedEvent (Realtime WebSocket)
        StockUpdatedEvent stock = StockUpdatedEvent.builder()
                .productId("PROD-001")
                .availableStock(45)
                .soldCount(55)
                .isFlashSaleActive(true)
                .build();
        String stockJson = objectMapper.writeValueAsString(stock);
        StockUpdatedEvent stockObj = objectMapper.readValue(stockJson, StockUpdatedEvent.class);
        assertThat(stockObj.getAvailableStock()).isEqualTo(45);
        assertThat(stockObj.getIsFlashSaleActive()).isTrue();
    }

    @Test
    @DisplayName("Test 4: Payment Domain Events Serialization Round-Trip")
    public void testPaymentDomainEvents() throws Exception {
        // 1. PaymentCompletedEvent
        PaymentCompletedEvent completed = PaymentCompletedEvent.builder()
                .paymentId("PAY-1001")
                .orderId("ORD-1001")
                .userId("usr-1001")
                .amount(new BigDecimal("1290000.50"))
                .paymentMethod("VNPAY_QR")
                .transactionReference("VNP-TXN-887766")
                .status(PaymentStatus.SUCCESS)
                .build();
        String completedJson = objectMapper.writeValueAsString(completed);
        PaymentCompletedEvent completedObj = objectMapper.readValue(completedJson, PaymentCompletedEvent.class);
        assertThat(completedObj.getTransactionReference()).isEqualTo("VNP-TXN-887766");
        assertThat(completedObj.getStatus()).isEqualTo(PaymentStatus.SUCCESS);

        // 2. PaymentFailedEvent
        PaymentFailedEvent failed = PaymentFailedEvent.builder()
                .paymentId("PAY-1002")
                .orderId("ORD-1002")
                .userId("usr-1002")
                .amount(new BigDecimal("1290000.50"))
                .failureReason("INSUFFICIENT_FUNDS")
                .status(PaymentStatus.FAILED)
                .build();
        String failedJson = objectMapper.writeValueAsString(failed);
        PaymentFailedEvent failedObj = objectMapper.readValue(failedJson, PaymentFailedEvent.class);
        assertThat(failedObj.getFailureReason()).isEqualTo("INSUFFICIENT_FUNDS");
        assertThat(failedObj.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    @DisplayName("Test 5: Notification Domain Events Serialization Round-Trip")
    public void testNotificationDomainEvents() throws Exception {
        NotificationEvent notification = NotificationEvent.builder()
                .notificationId("NOTIF-5544")
                .userId("usr-1001")
                .title("Đơn hàng Flash Sale thành công!")
                .message("Đơn hàng #ORD-1001 đã được xác nhận và đang đóng gói.")
                .notificationType(NotificationType.ORDER_STATUS_CHANGED)
                .targetChannel("/user/usr-1001/queue/notifications")
                .metadata(Map.of("orderId", "ORD-1001", "totalAmount", 1290000))
                .build();

        String json = objectMapper.writeValueAsString(notification);
        NotificationEvent notifObj = objectMapper.readValue(json, NotificationEvent.class);

        assertThat(notifObj.getTitle()).isEqualTo("Đơn hàng Flash Sale thành công!");
        assertThat(notifObj.getNotificationType()).isEqualTo(NotificationType.ORDER_STATUS_CHANGED);
        assertThat(notifObj.getMetadata().get("orderId")).isEqualTo("ORD-1001");
    }

    @Test
    @DisplayName("Test 6: Backward Compatibility with Unknown Properties")
    public void testBackwardCompatibility() throws Exception {
        // Giả lập Payload từ Producer có thêm trường mới mà Consumer phiên bản cũ chưa có
        String jsonWithExtraField = "{"
                + "\"orderId\":\"ORD-COMPAT-001\","
                + "\"userId\":\"usr-1\","
                + "\"productId\":\"PROD-001\","
                + "\"quantity\":1,"
                + "\"status\":\"PENDING\","
                + "\"experimentalFeatureFlag\":true,"
                + "\"futureV2SchemaField\":\"XYZ\""
                + "}";

        OrderCreatedEvent event = objectMapper.readValue(jsonWithExtraField, OrderCreatedEvent.class);
        assertThat(event).isNotNull();
        assertThat(event.getOrderId()).isEqualTo("ORD-COMPAT-001");
        assertThat(event.getStatus()).isEqualTo(OrderStatus.PENDING);
    }
}
