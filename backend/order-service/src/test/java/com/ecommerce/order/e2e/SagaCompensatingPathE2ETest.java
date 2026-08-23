package com.ecommerce.order.e2e;

import com.ecommerce.common.config.KafkaTopicConstants;
import com.ecommerce.common.event.BaseEvent;
import com.ecommerce.common.event.inventory.InventoryReservationFailedEvent;
import com.ecommerce.common.event.inventory.InventoryReservedEvent;
import com.ecommerce.common.event.inventory.InventoryRestoredEvent;
import com.ecommerce.common.event.order.OrderCreatedEvent;
import com.ecommerce.common.event.order.OrderStatus;
import com.ecommerce.common.event.payment.PaymentFailedEvent;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SagaCompensatingPathE2ETest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private EventPublisherService eventPublisherService;

    private OrderService orderService;

    // Giả lập cơ sở dữ liệu và hạ tầng phân tán
    private final List<Order> inMemoryOrderDb = new ArrayList<>();
    private final AtomicInteger mockStockDb = new AtomicInteger(10);
    private final List<String> publishedEventLog = new ArrayList<>();
    private final AtomicBoolean paymentProcessed = new AtomicBoolean(false);

    @BeforeEach
    public void setup() {
        inMemoryOrderDb.clear();
        publishedEventLog.clear();
        mockStockDb.set(10);
        paymentProcessed.set(false);

        orderService = new OrderServiceImpl(orderRepository, eventPublisherService);

        // Mock lưu Order
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
    @DisplayName("Compensating Test 1: Payment Failure triggers Compensating Stock Rollback (10 -> 9 -> 10)")
    public void testCompensatingPath_PaymentFailure_RollsBackStockAndUpdatesOrder() {
        String userId = "user_1002";
        String userEmail = "quang.fail@ecommerce.vn";
        String productId = "PROD-IPHONE-15-FLASH";
        String productTitle = "iPhone 15 Pro Max Flash Sale Edition";
        BigDecimal unitPrice = new BigDecimal("29990000");
        int quantity = 1;

        // =========================================================================
        // BƯỚC 1: Khách hàng tạo đơn -> order-service lưu Order(PENDING)
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
        String orderId = initialOrder.getOrderId();

        assertThat(initialOrder.getStatus()).isEqualTo(OrderStatus.PENDING);

        // =========================================================================
        // BƯỚC 2: inventory-service trừ giữ chỗ kho (10 -> 9)
        // =========================================================================
        int stockAfterReserve = mockStockDb.decrementAndGet();
        assertThat(stockAfterReserve).isEqualTo(9);

        InventoryReservedEvent inventoryEvent = InventoryReservedEvent.builder()
                .orderId(orderId)
                .productId(productId)
                .quantityReserved(quantity)
                .remainingStock(stockAfterReserve)
                .status("SUCCESS")
                .reservedAt(Instant.now())
                .build();

        orderService.handleInventoryReserved(inventoryEvent);
        Order orderAfterInv = inMemoryOrderDb.stream()
                .filter(o -> o.getOrderId().equals(orderId))
                .findFirst().orElseThrow();
        assertThat(orderAfterInv.getStatus()).isEqualTo(OrderStatus.INVENTORY_RESERVED);

        // =========================================================================
        // BƯỚC 3: payment-service xử lý thất bại (Thẻ không đủ số dư) -> Phát PaymentFailedEvent
        // =========================================================================
        PaymentFailedEvent paymentFailedEvent = PaymentFailedEvent.builder()
                .paymentId("PAY-FAILED-" + orderId)
                .orderId(orderId)
                .userId(userId)
                .amount(unitPrice)
                .failureReason("Tài khoản thẻ không đủ số dư giao dịch")
                .status(PaymentStatus.FAILED)
                .failedAt(Instant.now())
                .build();

        // =========================================================================
        // BƯỚC 4: inventory-service nhận PaymentFailedEvent -> Giao dịch bù trừ (9 -> 10)
        // =========================================================================
        int stockAfterCompensate = mockStockDb.addAndGet(quantity);
        assertThat(stockAfterCompensate).isEqualTo(10); // Phục hồi chính xác 100% kho ban đầu

        InventoryRestoredEvent restoredEvent = InventoryRestoredEvent.builder()
                .orderId(orderId)
                .productId(productId)
                .quantityRestored(quantity)
                .updatedStock(stockAfterCompensate)
                .reason("COMPENSATING_PAYMENT_FAILED")
                .restoredAt(Instant.now())
                .build();

        // =========================================================================
        // BƯỚC 5: order-service nhận PaymentFailedEvent -> Cập nhật PAYMENT_FAILED
        // =========================================================================
        orderService.handlePaymentFailed(paymentFailedEvent);

        Order finalOrder = inMemoryOrderDb.stream()
                .filter(o -> o.getOrderId().equals(orderId))
                .findFirst().orElseThrow();

        // =========================================================================
        // BƯỚC 6: Kiểm chứng Toàn vẹn Giao dịch Bù trừ (Compensating Invariants)
        // =========================================================================
        assertThat(finalOrder.getStatus()).isEqualTo(OrderStatus.PAYMENT_FAILED);
        assertThat(mockStockDb.get()).isEqualTo(10); // Kho được hoàn trả nguyên vẹn
        assertThat(paymentProcessed.get()).isFalse(); // Không có tiền nào bị trừ thành công
    }

    @Test
    @DisplayName("Compensating Test 2: Out of Stock rejects order immediately without payment")
    public void testCompensatingPath_OutOfStock_CancelsOrderImmediately() {
        String userId = "user_1003";
        String productId = "PROD-FLASH-IPHONE-15";
        mockStockDb.set(0); // Kho đã hết sạch hàng

        CreateOrderRequest request = CreateOrderRequest.builder()
                .userId(userId)
                .userEmail("out_of_stock@ecommerce.vn")
                .productId(productId)
                .productTitle("iPhone 15 Pro Max")
                .quantity(1)
                .unitPrice(new BigDecimal("29990000"))
                .build();

        OrderResponse initialOrder = orderService.createOrder(request);
        String orderId = initialOrder.getOrderId();

        // inventory-service phát hiện tồn kho = 0 -> Phát InventoryReservationFailedEvent
        InventoryReservationFailedEvent failEvent = InventoryReservationFailedEvent.builder()
                .orderId(orderId)
                .productId(productId)
                .requestedQuantity(1)
                .availableStock(0)
                .failureReason("Kho Flash Sale đã hết hàng")
                .failedAt(Instant.now())
                .build();

        // order-service nhận sự kiện hết hàng -> Hủy đơn ngay lập tức
        orderService.handleInventoryReservationFailed(failEvent);

        Order finalOrder = inMemoryOrderDb.stream()
                .filter(o -> o.getOrderId().equals(orderId))
                .findFirst().orElseThrow();

        assertThat(finalOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED_OUT_OF_STOCK);
        assertThat(mockStockDb.get()).isEqualTo(0);
        assertThat(paymentProcessed.get()).isFalse();
    }
}
