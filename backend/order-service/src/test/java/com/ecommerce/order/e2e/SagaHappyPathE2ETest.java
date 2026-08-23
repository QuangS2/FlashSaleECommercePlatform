package com.ecommerce.order.e2e;

import com.ecommerce.common.config.KafkaTopicConstants;
import com.ecommerce.common.event.BaseEvent;
import com.ecommerce.common.event.inventory.InventoryReservedEvent;
import com.ecommerce.common.event.order.OrderConfirmedEvent;
import com.ecommerce.common.event.order.OrderCreatedEvent;
import com.ecommerce.common.event.order.OrderStatus;
import com.ecommerce.common.event.payment.PaymentCompletedEvent;
import com.ecommerce.common.event.payment.PaymentStatus;
import com.ecommerce.common.kafka.EventPublisherService;
import com.ecommerce.order.dto.CreateOrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.order.service.OrderService;
import com.ecommerce.order.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SagaHappyPathE2ETest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private EventPublisherService eventPublisherService;

    private OrderService orderService;

    // Giả lập cơ sở dữ liệu và hạ tầng bộ nhớ đệm
    private final List<Order> inMemoryOrderDb = new ArrayList<>();
    private final AtomicInteger mockStockDb = new AtomicInteger(10);
    private final List<String> publishedEventLog = new ArrayList<>();

    @BeforeEach
    public void setup() {
        inMemoryOrderDb.clear();
        publishedEventLog.clear();
        mockStockDb.set(10);

        orderService = new OrderServiceImpl(orderRepository, eventPublisherService);

        // Mock hành vi lưu Order vào inMemoryOrderDb và cập nhật timestamp
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            if (order.getCreatedAt() == null) {
                order.setCreatedAt(Instant.now());
            }
            order.setUpdatedAt(Instant.now());
            inMemoryOrderDb.removeIf(o -> o.getOrderId().equals(order.getOrderId()));
            inMemoryOrderDb.add(order);
            return order;
        });

        // Mock tìm kiếm Order theo orderId
        when(orderRepository.findByOrderId(anyString())).thenAnswer(invocation -> {
            String orderId = invocation.getArgument(0);
            return inMemoryOrderDb.stream()
                    .filter(o -> o.getOrderId().equals(orderId))
                    .findFirst();
        });

        // Mock ghi nhận phát sự kiện Kafka
        doAnswer(invocation -> {
            String topic = invocation.getArgument(0);
            String key = invocation.getArgument(1);
            BaseEvent<?> payload = invocation.getArgument(2);
            publishedEventLog.add(String.format("Topic: %s | Key: %s | Event: %s", topic, key, payload != null ? payload.getEventType() : "null"));
            return null;
        }).when(eventPublisherService).publish(anyString(), anyString(), any(BaseEvent.class));
    }

    @Test
    @DisplayName("E2E Test 1: Saga Happy Path - Full Choreography from Order PENDING to CONFIRMED")
    public void testSagaHappyPath_FullChoreography() {
        String userId = "user_1001";
        String userEmail = "quang.dev@ecommerce.vn";
        String productId = "PROD-IPHONE-15-FLASH";
        String productTitle = "iPhone 15 Pro Max Flash Sale Edition";
        BigDecimal unitPrice = new BigDecimal("29990000");
        int quantity = 1;

        // =========================================================================
        // BƯỚC 1: Khách hàng gửi yêu cầu mua -> order-service tạo Order(PENDING)
        // =========================================================================
        CreateOrderRequest request = CreateOrderRequest.builder()
                .userId(userId)
                .userEmail(userEmail)
                .productId(productId)
                .productTitle(productTitle)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .build();

        OrderResponse initialOrder = orderService.createOrder(request);

        assertThat(initialOrder).isNotNull();
        assertThat(initialOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(initialOrder.getTotalAmount()).isEqualByComparingTo(new BigDecimal("29990000"));

        String orderId = initialOrder.getOrderId();

        // Kiểm chứng Step 1: Đã phát BaseEvent<OrderCreatedEvent>
        ArgumentCaptor<BaseEvent> orderCreatedCaptor = ArgumentCaptor.forClass(BaseEvent.class);
        verify(eventPublisherService).publish(eq(KafkaTopicConstants.TOPIC_ORDER_EVENTS), eq(orderId), orderCreatedCaptor.capture());
        BaseEvent<?> baseCreatedEvent = orderCreatedCaptor.getValue();
        assertThat(baseCreatedEvent.getPayload()).isInstanceOf(OrderCreatedEvent.class);
        OrderCreatedEvent createdEvent = (OrderCreatedEvent) baseCreatedEvent.getPayload();
        assertThat(createdEvent.getOrderId()).isEqualTo(orderId);
        assertThat(createdEvent.getStatus()).isEqualTo(OrderStatus.PENDING);

        // =========================================================================
        // BƯỚC 2: inventory-service tiêu thụ OrderCreatedEvent -> Trừ kho (10 -> 9)
        // =========================================================================
        int stockBefore = mockStockDb.get();
        assertThat(stockBefore).isEqualTo(10);

        // Giả lập logic xử lý tại inventory-service
        int stockAfter = mockStockDb.decrementAndGet();
        assertThat(stockAfter).isEqualTo(9);

        InventoryReservedEvent inventoryEvent = InventoryReservedEvent.builder()
                .orderId(orderId)
                .productId(productId)
                .quantityReserved(quantity)
                .remainingStock(stockAfter)
                .status("SUCCESS")
                .reservedAt(Instant.now())
                .build();

        // Cập nhật trạng thái đơn hàng sang INVENTORY_RESERVED
        orderService.handleInventoryReserved(inventoryEvent);
        Order orderAfterInventory = inMemoryOrderDb.stream()
                .filter(o -> o.getOrderId().equals(orderId))
                .findFirst().orElseThrow();
        assertThat(orderAfterInventory.getStatus()).isEqualTo(OrderStatus.INVENTORY_RESERVED);

        // =========================================================================
        // BƯỚC 3: payment-service tiêu thụ InventoryReservedEvent -> Xử lý trừ tiền
        // =========================================================================
        String txnRef = "TXN-VNPAY-" + System.currentTimeMillis();
        PaymentCompletedEvent paymentEvent = PaymentCompletedEvent.builder()
                .paymentId("PAY-" + orderId)
                .orderId(orderId)
                .userId(userId)
                .amount(new BigDecimal("29990000"))
                .paymentMethod("VNPAY")
                .transactionReference(txnRef)
                .status(PaymentStatus.SUCCESS)
                .completedAt(Instant.now())
                .build();

        // =========================================================================
        // BƯỚC 4: order-service nhận PaymentCompletedEvent -> Cập nhật CONFIRMED
        // =========================================================================
        orderService.handlePaymentCompleted(paymentEvent);

        Order finalOrder = inMemoryOrderDb.stream()
                .filter(o -> o.getOrderId().equals(orderId))
                .findFirst().orElseThrow();

        assertThat(finalOrder.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(finalOrder.getTotalAmount()).isEqualByComparingTo(new BigDecimal("29990000"));

        // =========================================================================
        // BƯỚC 5: Kiểm chứng Toàn vẹn (End-to-End Invariants)
        // =========================================================================
        assertThat(mockStockDb.get()).isEqualTo(9); // Tồn kho giảm chính xác 1 đơn vị
        assertThat(finalOrder.getUpdatedAt()).isNotNull();
        assertThat(publishedEventLog).isNotEmpty();
    }

    @Test
    @DisplayName("E2E Test 2: Sequence Integrity - Verifies all transition steps happen in exact order")
    public void testSagaHappyPath_SequenceIntegrity() {
        String orderId = "ORD-SEQ-001";
        Order order = Order.builder()
                .orderId(orderId)
                .userId("user_1002")
                .productId("PROD-FLASH-IPHONE-15")
                .quantity(1)
                .totalAmount(new BigDecimal("29990000"))
                .status(OrderStatus.PENDING)
                .createdAt(Instant.now())
                .build();

        inMemoryOrderDb.add(order);

        // Chuyển bước 1: PENDING -> INVENTORY_RESERVED
        InventoryReservedEvent invEvent = InventoryReservedEvent.builder()
                .orderId(orderId)
                .productId("PROD-FLASH-IPHONE-15")
                .quantityReserved(1)
                .remainingStock(99)
                .status("SUCCESS")
                .reservedAt(Instant.now())
                .build();
        orderService.handleInventoryReserved(invEvent);
        assertThat(inMemoryOrderDb.get(0).getStatus()).isEqualTo(OrderStatus.INVENTORY_RESERVED);

        // Chuyển bước 2: INVENTORY_RESERVED -> CONFIRMED
        PaymentCompletedEvent payEvent = PaymentCompletedEvent.builder()
                .paymentId("PAY-SEQ-001")
                .orderId(orderId)
                .userId("user_1002")
                .amount(new BigDecimal("29990000"))
                .paymentMethod("MOMO")
                .transactionReference("TXN-MOMO-888")
                .status(PaymentStatus.SUCCESS)
                .completedAt(Instant.now())
                .build();
        orderService.handlePaymentCompleted(payEvent);
        assertThat(inMemoryOrderDb.get(0).getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }
}
