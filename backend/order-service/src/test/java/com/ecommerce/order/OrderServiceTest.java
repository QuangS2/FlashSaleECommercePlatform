package com.ecommerce.order;

import com.ecommerce.common.config.KafkaTopicConstants;
import com.ecommerce.common.event.BaseEvent;
import com.ecommerce.common.event.inventory.InventoryReservationFailedEvent;
import com.ecommerce.common.event.inventory.InventoryReservedEvent;
import com.ecommerce.common.event.order.OrderStatus;
import com.ecommerce.common.event.payment.PaymentCompletedEvent;
import com.ecommerce.common.event.payment.PaymentFailedEvent;
import com.ecommerce.common.kafka.EventPublisherService;
import com.ecommerce.order.dto.CreateOrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.order.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private EventPublisherService eventPublisherService;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    @DisplayName("Test 1: createOrder - Creates Order with PENDING status and publishes OrderCreatedEvent")
    public void testCreateOrder() {
        CreateOrderRequest request = CreateOrderRequest.builder()
                .userId("user_1001")
                .userEmail("quang@ecommerce.vn")
                .productId("PROD-IPHONE-15")
                .productTitle("iPhone 15 Pro Max")
                .quantity(2)
                .unitPrice(new BigDecimal("29990000"))
                .build();

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            order.setCreatedAt(Instant.now());
            order.setUpdatedAt(Instant.now());
            return order;
        });

        OrderResponse response = orderService.createOrder(request);

        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).startsWith("ORD-");
        assertThat(response.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(response.getTotalAmount()).isEqualByComparingTo(new BigDecimal("59980000"));

        verify(orderRepository).save(any(Order.class));
        verify(eventPublisherService).publish(eq(KafkaTopicConstants.TOPIC_ORDER_EVENTS), eq(response.getOrderId()), any(BaseEvent.class));
    }

    @Test
    @DisplayName("Test 2: handleInventoryReserved - Updates status to INVENTORY_RESERVED")
    public void testHandleInventoryReserved() {
        String orderId = "ORD-TEST-001";
        Order existingOrder = Order.builder()
                .orderId(orderId)
                .status(OrderStatus.PENDING)
                .build();

        when(orderRepository.findByOrderId(orderId)).thenReturn(Optional.of(existingOrder));

        InventoryReservedEvent event = InventoryReservedEvent.builder()
                .orderId(orderId)
                .productId("PROD-1")
                .quantityReserved(2)
                .remainingStock(98)
                .build();

        orderService.handleInventoryReserved(event);

        assertThat(existingOrder.getStatus()).isEqualTo(OrderStatus.INVENTORY_RESERVED);
        verify(orderRepository).save(existingOrder);
    }

    @Test
    @DisplayName("Test 3: handleInventoryReservationFailed - Cancels order with CANCELLED_OUT_OF_STOCK")
    public void testHandleInventoryReservationFailed() {
        String orderId = "ORD-TEST-002";
        Order existingOrder = Order.builder()
                .orderId(orderId)
                .status(OrderStatus.PENDING)
                .build();

        when(orderRepository.findByOrderId(orderId)).thenReturn(Optional.of(existingOrder));

        InventoryReservationFailedEvent event = InventoryReservationFailedEvent.builder()
                .orderId(orderId)
                .productId("PROD-1")
                .requestedQuantity(2)
                .failureReason("Kho Flash Sale đã hết hàng")
                .build();

        orderService.handleInventoryReservationFailed(event);

        assertThat(existingOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED_OUT_OF_STOCK);
        assertThat(existingOrder.getCancelReason()).isEqualTo("Kho Flash Sale đã hết hàng");
        verify(orderRepository).save(existingOrder);
    }

    @Test
    @DisplayName("Test 4: handlePaymentCompleted - Confirms order and publishes OrderConfirmedEvent")
    public void testHandlePaymentCompleted() {
        String orderId = "ORD-TEST-003";
        Order existingOrder = Order.builder()
                .orderId(orderId)
                .status(OrderStatus.INVENTORY_RESERVED)
                .totalAmount(new BigDecimal("1000000"))
                .build();

        when(orderRepository.findByOrderId(orderId)).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> {
            Order o = i.getArgument(0);
            o.setUpdatedAt(Instant.now());
            return o;
        });

        PaymentCompletedEvent event = PaymentCompletedEvent.builder()
                .paymentId("PAY-TXN-12345")
                .orderId(orderId)
                .amount(new BigDecimal("1000000"))
                .paymentMethod("VNPAY")
                .build();

        orderService.handlePaymentCompleted(event);

        assertThat(existingOrder.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(existingOrder.getPaymentId()).isEqualTo("PAY-TXN-12345");
        verify(orderRepository).save(existingOrder);
        verify(eventPublisherService).publish(eq(KafkaTopicConstants.TOPIC_ORDER_EVENTS), eq(orderId), any(BaseEvent.class));
    }

    @Test
    @DisplayName("Test 5: handlePaymentFailed - Sets PAYMENT_FAILED and publishes OrderCancelledEvent for rollback")
    public void testHandlePaymentFailed() {
        String orderId = "ORD-TEST-004";
        Order existingOrder = Order.builder()
                .orderId(orderId)
                .productId("PROD-1")
                .quantity(1)
                .status(OrderStatus.INVENTORY_RESERVED)
                .build();

        when(orderRepository.findByOrderId(orderId)).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> {
            Order o = i.getArgument(0);
            o.setUpdatedAt(Instant.now());
            return o;
        });

        PaymentFailedEvent event = PaymentFailedEvent.builder()
                .orderId(orderId)
                .failureReason("Tài khoản thẻ không đủ số dư")
                .build();

        orderService.handlePaymentFailed(event);

        assertThat(existingOrder.getStatus()).isEqualTo(OrderStatus.PAYMENT_FAILED);
        assertThat(existingOrder.getCancelReason()).isEqualTo("Tài khoản thẻ không đủ số dư");
        verify(orderRepository).save(existingOrder);
        verify(eventPublisherService).publish(eq(KafkaTopicConstants.TOPIC_ORDER_EVENTS), eq(orderId), any(BaseEvent.class));
    }

    @Test
    @DisplayName("Test 6: getOrderByOrderId - Returns order if found, throws if not found")
    public void testGetOrderByOrderId() {
        String orderId = "ORD-FOUND";
        Order order = Order.builder().orderId(orderId).status(OrderStatus.CONFIRMED).build();
        when(orderRepository.findByOrderId(orderId)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.getOrderByOrderId(orderId);
        assertThat(response.getOrderId()).isEqualTo(orderId);

        when(orderRepository.findByOrderId("ORD-NOT-FOUND")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> orderService.getOrderByOrderId("ORD-NOT-FOUND"));
    }
}
