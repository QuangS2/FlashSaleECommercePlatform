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
import java.util.List;
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

    @Mock
    private com.ecommerce.order.infrastructure.persistence.repository.SpringDataOutboxEventRepository outboxEventRepository;

    @Mock
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Mock
    private com.ecommerce.order.infrastructure.redis.RedisLuaService redisLuaService;

    @InjectMocks
    private OrderApplicationService orderApplicationService;

    @Test
    void testCreateOrderSuccess() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId("user_1");
        request.setUserEmail("user@example.com");
        request.setProductId("prod_1");
        request.setProductTitle("Product 1");
        request.setQuantity(2);
        request.setUnitPrice(new BigDecimal("150.0"));

        when(orderRepositoryPort.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"orderId\":\"test\"}");

        OrderResponse response = orderApplicationService.createOrder(request);

        assertNotNull(response.getOrderId());
        assertEquals("user_1", response.getUserId());
        assertEquals(0, new BigDecimal("300.0").compareTo(response.getTotalAmount())); // 2 * 150

        verify(orderRepositoryPort, times(1)).save(any(Order.class));
        verify(outboxEventRepository, times(1)).save(any());
        verify(eventPublisherPort, times(1)).publishOrderCreatedEvent(anyString(), any(BaseEvent.class));
    }

    @Test
    void testCreateOrderOutboxExceptionHandled() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId("user_1");
        request.setUserEmail("user@example.com");
        request.setProductId("prod_1");
        request.setProductTitle("Product 1");
        request.setQuantity(1);
        request.setUnitPrice(new BigDecimal("100.0"));

        when(orderRepositoryPort.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(objectMapper.writeValueAsString(any())).thenThrow(new RuntimeException("JSON serialization error"));

        OrderResponse response = orderApplicationService.createOrder(request);

        assertNotNull(response.getOrderId());
        verify(eventPublisherPort, times(1)).publishOrderCreatedEvent(anyString(), any(BaseEvent.class));
    }

    @Test
    void testGetOrdersByUserId() {
        Order mockOrder = Order.builder().orderId("ORD-1").userId("user-1").build();
        when(orderRepositoryPort.findByUserIdOrderByCreatedAtDesc("user-1")).thenReturn(List.of(mockOrder));

        List<OrderResponse> responses = orderApplicationService.getOrdersByUserId("user-1");
        assertEquals(1, responses.size());
        assertEquals("ORD-1", responses.get(0).getOrderId());
    }

    @Test
    void testGetOrdersByUserEmail() {
        Order mockOrder = Order.builder().orderId("ORD-2").userEmail("u@e.com").build();
        when(orderRepositoryPort.findByUserEmailOrderByCreatedAtDesc("u@e.com")).thenReturn(List.of(mockOrder));

        List<OrderResponse> responses = orderApplicationService.getOrdersByUserEmail("u@e.com");
        assertEquals(1, responses.size());
        assertEquals("ORD-2", responses.get(0).getOrderId());
    }

    @Test
    void testGetOrderByOrderIdSuccess() {
        Order mockOrder = Order.builder().orderId("ORD-3").userId("u3").build();
        when(orderRepositoryPort.findByOrderId("ORD-3")).thenReturn(Optional.of(mockOrder));

        OrderResponse response = orderApplicationService.getOrderByOrderId("ORD-3");
        assertEquals("ORD-3", response.getOrderId());
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

    @Test
    void testCreateFlashSaleOrder_Success() throws Exception {
        com.ecommerce.order.dto.FlashSaleOrderRequest request = com.ecommerce.order.dto.FlashSaleOrderRequest.builder()
                .saleId(1L)
                .itemId(101L)
                .quantity(1)
                .unitPrice(new BigDecimal("99.00"))
                .userEmail("cust@test.com")
                .idempotencyKey("idemp-123")
                .build();

        when(redisLuaService.reserveStock(1L, 101L, "user-1", 1, 300)).thenReturn(true);
        when(orderRepositoryPort.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"orderId\":\"flash-123\"}");

        OrderResponse response = orderApplicationService.createFlashSaleOrder(request, "user-1");

        assertNotNull(response);
        assertEquals("user-1", response.getUserId());
        assertEquals(com.ecommerce.common.event.order.OrderStatus.PENDING, response.getStatus());
        verify(redisLuaService, times(1)).reserveStock(1L, 101L, "user-1", 1, 300);
        verify(orderRepositoryPort, times(1)).save(any(Order.class));
        verify(outboxEventRepository, times(1)).save(any());
    }

    @Test
    void testCreateFlashSaleOrder_OutOfStock() {
        com.ecommerce.order.dto.FlashSaleOrderRequest request = com.ecommerce.order.dto.FlashSaleOrderRequest.builder()
                .saleId(1L)
                .itemId(101L)
                .quantity(1)
                .build();

        when(redisLuaService.reserveStock(1L, 101L, "user-1", 1, 300)).thenReturn(false);

        assertThrows(com.ecommerce.order.domain.exception.StockReservationException.class, () ->
                orderApplicationService.createFlashSaleOrder(request, "user-1"));

        verify(orderRepositoryPort, never()).save(any());
    }

    @Test
    void testCreateFlashSaleOrder_DbError_CompensatesRedis() {
        com.ecommerce.order.dto.FlashSaleOrderRequest request = com.ecommerce.order.dto.FlashSaleOrderRequest.builder()
                .saleId(1L)
                .itemId(101L)
                .quantity(1)
                .build();

        when(redisLuaService.reserveStock(1L, 101L, "user-1", 1, 300)).thenReturn(true);
        when(orderRepositoryPort.save(any(Order.class))).thenThrow(new RuntimeException("DB Connection timeout"));

        assertThrows(RuntimeException.class, () ->
                orderApplicationService.createFlashSaleOrder(request, "user-1"));

        // Verify compensation: releases reservation on Redis immediately
        verify(redisLuaService, times(1)).releaseReservation(1L, 101L, "user-1", 1);
    }

    @Test
    void testCreateFlashSaleOrder_NullUnitPriceAndEmail() throws Exception {
        com.ecommerce.order.dto.FlashSaleOrderRequest request = com.ecommerce.order.dto.FlashSaleOrderRequest.builder()
                .saleId(2L)
                .itemId(202L)
                .quantity(2)
                .build();

        when(redisLuaService.reserveStock(2L, 202L, "cust-null", 2, 300)).thenReturn(true);
        when(orderRepositoryPort.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        OrderResponse response = orderApplicationService.createFlashSaleOrder(request, "cust-null");

        assertNotNull(response);
        assertEquals("cust-null", response.getUserId());
        assertEquals(BigDecimal.ZERO, response.getTotalAmount());
    }

    @Test
    void testCreateFlashSaleOrder_OutboxSerializationError() throws Exception {
        com.ecommerce.order.dto.FlashSaleOrderRequest request = com.ecommerce.order.dto.FlashSaleOrderRequest.builder()
                .saleId(3L)
                .itemId(303L)
                .quantity(1)
                .unitPrice(new BigDecimal("50.00"))
                .build();

        when(redisLuaService.reserveStock(3L, 303L, "cust-err", 1, 300)).thenReturn(true);
        when(orderRepositoryPort.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
        when(objectMapper.writeValueAsString(any())).thenThrow(new RuntimeException("Serialization failure"));

        OrderResponse response = orderApplicationService.createFlashSaleOrder(request, "cust-err");

        assertNotNull(response);
        assertEquals("cust-err", response.getUserId());
        assertEquals(com.ecommerce.common.event.order.OrderStatus.PENDING, response.getStatus());
    }
}
