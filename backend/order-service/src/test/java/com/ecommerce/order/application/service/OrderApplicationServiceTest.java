package com.ecommerce.order.application.service;

import com.ecommerce.common.event.BaseEvent;
import com.ecommerce.order.domain.entity.Order;
import com.ecommerce.order.domain.port.out.EventPublisherPort;
import com.ecommerce.order.domain.port.out.OrderRepositoryPort;
import com.ecommerce.order.dto.CreateOrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderApplicationServiceTest {

    @Mock
    private OrderRepositoryPort orderRepositoryPort;

    @Mock
    private EventPublisherPort eventPublisherPort;

    @InjectMocks
    private OrderApplicationService orderApplicationService;

    @Test
    void testCreateOrderSuccess() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId("user_1");
        request.setUserEmail("user@example.com");
        request.setProductId("prod_1");
        request.setProductTitle("Product 1");
        request.setQuantity(2);
        request.setUnitPrice(new BigDecimal("150.0"));

        when(orderRepositoryPort.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderApplicationService.createOrder(request);

        assertNotNull(response.getOrderId());
        assertEquals("user_1", response.getUserId());
        assertEquals(0, new BigDecimal("300.0").compareTo(response.getTotalAmount())); // 2 * 150

        verify(orderRepositoryPort, times(1)).save(any(Order.class));
        verify(eventPublisherPort, times(1)).publishOrderCreatedEvent(anyString(), any(BaseEvent.class));
    }

    @Test
    void testGetOrderNotFound() {
        when(orderRepositoryPort.findByOrderId(anyString())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                orderApplicationService.getOrderByOrderId("order_xyz"));

        assertEquals("Kh\u00f4ng t\u00ecm th\u1ea5y \u0111\u01a1n h\u00e0ng v\u1edbi m\u00e3: order_xyz", exception.getMessage());
    }

    @Test
    void testGetOrderByOrderId_Success() {
        Order mockOrder = Order.builder().orderId("ORD-123").status(com.ecommerce.common.event.order.OrderStatus.PENDING).build();
        when(orderRepositoryPort.findByOrderId("ORD-123")).thenReturn(Optional.of(mockOrder));

        OrderResponse response = orderApplicationService.getOrderByOrderId("ORD-123");

        assertNotNull(response);
        assertEquals("ORD-123", response.getOrderId());
    }

    @Test
    void testGetOrdersByUserId() {
        Order mockOrder = Order.builder().orderId("ORD-123").status(com.ecommerce.common.event.order.OrderStatus.PENDING).build();
        when(orderRepositoryPort.findByUserIdOrderByCreatedAtDesc("user_1"))
                .thenReturn(java.util.Arrays.asList(mockOrder));

        java.util.List<OrderResponse> responseList = orderApplicationService.getOrdersByUserId("user_1");

        assertFalse(responseList.isEmpty());
        assertEquals("ORD-123", responseList.get(0).getOrderId());
    }

    @Test
    void testHandleInventoryReserved_Success() {
        Order mockOrder = Order.builder().orderId("ORD-123").status(com.ecommerce.common.event.order.OrderStatus.PENDING).build();
        when(orderRepositoryPort.findByOrderId("ORD-123")).thenReturn(Optional.of(mockOrder));

        com.ecommerce.common.event.inventory.InventoryReservedEvent event = 
                com.ecommerce.common.event.inventory.InventoryReservedEvent.builder().orderId("ORD-123").build();

        orderApplicationService.handleInventoryReserved(event);

        verify(orderRepositoryPort, times(1)).save(any(Order.class));
        assertEquals(com.ecommerce.common.event.order.OrderStatus.INVENTORY_RESERVED, mockOrder.getStatus());
    }

    @Test
    void testHandleInventoryReserved_Exception() {
        Order mockOrder = Order.builder().orderId("ORD-123").status(com.ecommerce.common.event.order.OrderStatus.CONFIRMED).build(); // Invalid state for reservation
        when(orderRepositoryPort.findByOrderId("ORD-123")).thenReturn(Optional.of(mockOrder));

        com.ecommerce.common.event.inventory.InventoryReservedEvent event = 
                com.ecommerce.common.event.inventory.InventoryReservedEvent.builder().orderId("ORD-123").build();

        orderApplicationService.handleInventoryReserved(event);

        verify(orderRepositoryPort, never()).save(any(Order.class));
    }

    @Test
    void testHandleInventoryReservationFailed() {
        Order mockOrder = Order.builder().orderId("ORD-123").status(com.ecommerce.common.event.order.OrderStatus.PENDING).build();
        when(orderRepositoryPort.findByOrderId("ORD-123")).thenReturn(Optional.of(mockOrder));

        com.ecommerce.common.event.inventory.InventoryReservationFailedEvent event = 
                com.ecommerce.common.event.inventory.InventoryReservationFailedEvent.builder()
                .orderId("ORD-123").failureReason("Out of stock").build();

        orderApplicationService.handleInventoryReservationFailed(event);

        verify(orderRepositoryPort, times(1)).save(any(Order.class));
        assertEquals(com.ecommerce.common.event.order.OrderStatus.CANCELLED_OUT_OF_STOCK, mockOrder.getStatus());
    }

    @Test
    void testHandlePaymentCompleted() {
        Order mockOrder = Order.builder().orderId("ORD-123").status(com.ecommerce.common.event.order.OrderStatus.INVENTORY_RESERVED).build();
        when(orderRepositoryPort.findByOrderId("ORD-123")).thenReturn(Optional.of(mockOrder));
        when(orderRepositoryPort.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        com.ecommerce.common.event.payment.PaymentCompletedEvent event = 
                com.ecommerce.common.event.payment.PaymentCompletedEvent.builder()
                .orderId("ORD-123").paymentId("PAY-123").build();

        orderApplicationService.handlePaymentCompleted(event);

        verify(orderRepositoryPort, times(1)).save(any(Order.class));
        verify(eventPublisherPort, times(1)).publishOrderConfirmedEvent(anyString(), any(BaseEvent.class));
        assertEquals(com.ecommerce.common.event.order.OrderStatus.CONFIRMED, mockOrder.getStatus());
        assertEquals("PAY-123", mockOrder.getPaymentId());
    }

    @Test
    void testHandlePaymentFailed() {
        Order mockOrder = Order.builder().orderId("ORD-123").status(com.ecommerce.common.event.order.OrderStatus.INVENTORY_RESERVED).build();
        when(orderRepositoryPort.findByOrderId("ORD-123")).thenReturn(Optional.of(mockOrder));
        when(orderRepositoryPort.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        com.ecommerce.common.event.payment.PaymentFailedEvent event = 
                com.ecommerce.common.event.payment.PaymentFailedEvent.builder()
                .orderId("ORD-123").failureReason("Insufficient balance").build();

        orderApplicationService.handlePaymentFailed(event);

        verify(orderRepositoryPort, times(1)).save(any(Order.class));
        verify(eventPublisherPort, times(1)).publishOrderCancelledEvent(anyString(), any(BaseEvent.class));
        assertEquals(com.ecommerce.common.event.order.OrderStatus.PAYMENT_FAILED, mockOrder.getStatus());
    }
}
