package com.ecommerce.order.infrastructure.persistence.adapter;

import com.ecommerce.order.domain.entity.Order;
import com.ecommerce.order.infrastructure.persistence.entity.OrderEntity;
import com.ecommerce.order.infrastructure.persistence.repository.SpringDataOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderJpaAdapterTest {

    @Mock
    private SpringDataOrderRepository springDataOrderRepository;

    @InjectMocks
    private OrderJpaAdapter orderJpaAdapter;

    private OrderEntity mockEntity;
    private Order mockDomain;

    @BeforeEach
    void setUp() {
        mockEntity = OrderEntity.builder()
                .id(1L)
                .orderId("ORD-123")
                .userId("user_1")
                .productId("prod_1")
                .quantity(1)
                .totalAmount(new BigDecimal("100.00"))
                .status(com.ecommerce.common.event.order.OrderStatus.PENDING)
                .build();

        mockDomain = Order.builder()
                .id(1L)
                .orderId("ORD-123")
                .userId("user_1")
                .productId("prod_1")
                .quantity(1)
                .totalAmount(new BigDecimal("100.00"))
                .status(com.ecommerce.common.event.order.OrderStatus.PENDING)
                .build();
    }

    @Test
    void testSave() {
        when(springDataOrderRepository.save(any(OrderEntity.class))).thenReturn(mockEntity);

        Order result = orderJpaAdapter.save(mockDomain);

        assertNotNull(result);
        assertEquals("ORD-123", result.getOrderId());
        verify(springDataOrderRepository, times(1)).save(any(OrderEntity.class));
    }

    @Test
    void testFindByOrderId_Found() {
        when(springDataOrderRepository.findByOrderId("ORD-123")).thenReturn(Optional.of(mockEntity));

        Optional<Order> result = orderJpaAdapter.findByOrderId("ORD-123");

        assertTrue(result.isPresent());
        assertEquals("ORD-123", result.get().getOrderId());
        verify(springDataOrderRepository, times(1)).findByOrderId("ORD-123");
    }

    @Test
    void testFindByOrderId_NotFound() {
        when(springDataOrderRepository.findByOrderId("ORD-123")).thenReturn(Optional.empty());

        Optional<Order> result = orderJpaAdapter.findByOrderId("ORD-123");

        assertFalse(result.isPresent());
        verify(springDataOrderRepository, times(1)).findByOrderId("ORD-123");
    }

    @Test
    void testFindByUserIdOrderByCreatedAtDesc() {
        when(springDataOrderRepository.findByUserIdOrderByCreatedAtDesc("user_1")).thenReturn(Arrays.asList(mockEntity));

        List<Order> result = orderJpaAdapter.findByUserIdOrderByCreatedAtDesc("user_1");

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("ORD-123", result.get(0).getOrderId());
        verify(springDataOrderRepository, times(1)).findByUserIdOrderByCreatedAtDesc("user_1");
    }
}
