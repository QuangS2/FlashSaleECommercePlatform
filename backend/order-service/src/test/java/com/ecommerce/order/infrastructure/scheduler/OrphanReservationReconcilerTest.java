package com.ecommerce.order.infrastructure.scheduler;

import com.ecommerce.common.event.order.OrderStatus;
import com.ecommerce.order.domain.entity.Order;
import com.ecommerce.order.domain.port.out.OrderRepositoryPort;
import com.ecommerce.order.infrastructure.persistence.repository.SpringDataOutboxEventRepository;
import com.ecommerce.order.infrastructure.redis.RedisLuaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrphanReservationReconcilerTest {

    @Mock
    private OrderRepositoryPort orderRepositoryPort;

    @Mock
    private RedisLuaService redisLuaService;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private SpringDataOutboxEventRepository outboxEventRepository;

    @Mock
    private ObjectMapper objectMapper;

    private OrphanReservationReconciler reconciler;

    @BeforeEach
    void setUp() {
        reconciler = new OrphanReservationReconciler(
                orderRepositoryPort,
                redisLuaService,
                kafkaTemplate,
                outboxEventRepository,
                objectMapper
        );
    }

    @Test
    void testReconcileOrphanReservations_ExpiredOrder() {
        Order expiredOrder = Order.builder()
                .orderId("ORD-OLD")
                .userId("user-1")
                .productId("101")
                .quantity(1)
                .unitPrice(new BigDecimal("100.00"))
                .status(OrderStatus.PENDING)
                .createdAt(Instant.now().minusSeconds(400)) // Older than 300s
                .build();

        Order recentOrder = Order.builder()
                .orderId("ORD-NEW")
                .userId("user-2")
                .productId("102")
                .quantity(1)
                .unitPrice(new BigDecimal("100.00"))
                .status(OrderStatus.PENDING)
                .createdAt(Instant.now().minusSeconds(60)) // Only 60s
                .build();

        when(orderRepositoryPort.findAll()).thenReturn(List.of(expiredOrder, recentOrder));
        when(orderRepositoryPort.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        reconciler.reconcileOrphanReservations();

        // Expired order should be cancelled (EXPIRED/CANCELLED)
        assertEquals(OrderStatus.CANCELLED, expiredOrder.getStatus());
        verify(redisLuaService, times(1)).releaseReservation(1L, 101L, "user-1", 1);
        verify(kafkaTemplate, times(1)).send(anyString(), eq("ORD-OLD"), anyString());
        verify(outboxEventRepository, times(1)).save(any());

        // Recent order should remain untouched
        assertEquals(OrderStatus.PENDING, recentOrder.getStatus());
    }

    @Test
    void testReconcileOrphanReservations_EmptyList() {
        when(orderRepositoryPort.findAll()).thenReturn(List.of());

        reconciler.reconcileOrphanReservations();

        verifyNoInteractions(redisLuaService);
        verifyNoInteractions(kafkaTemplate);
    }
}
