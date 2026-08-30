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
}
