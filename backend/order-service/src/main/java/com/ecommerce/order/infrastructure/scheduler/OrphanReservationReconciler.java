package com.ecommerce.order.infrastructure.scheduler;

import com.ecommerce.common.config.KafkaTopicConstants;
import com.ecommerce.common.event.order.OrderStatus;
import com.ecommerce.order.domain.entity.Order;
import com.ecommerce.order.domain.port.out.OrderRepositoryPort;
import com.ecommerce.order.infrastructure.persistence.entity.OutboxEventEntity;
import com.ecommerce.order.infrastructure.persistence.repository.SpringDataOutboxEventRepository;
import com.ecommerce.order.infrastructure.redis.RedisLuaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Scheduled job to reconcile and release orphan stock reservations on Redis (Mục 4.2.2).
 * Runs every 60 seconds, sweeps PENDING orders older than 300 seconds, marks them EXPIRED,
 * restores Redis available stock and emits OrderReservationExpiredEvent.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrphanReservationReconciler {

    private final OrderRepositoryPort orderRepositoryPort;
    private final RedisLuaService redisLuaService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final SpringDataOutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 60000)
    public void reconcileOrphanReservations() {
        Instant threshold = Instant.now().minusSeconds(300); // 300s reservation window
        List<Order> orders = orderRepositoryPort.findAll();
        if (orders == null || orders.isEmpty()) {
            return;
        }

        int expiredCount = 0;
        for (Order order : orders) {
            if (order.getStatus() == OrderStatus.PENDING && order.getCreatedAt() != null && order.getCreatedAt().isBefore(threshold)) {
                try {
                    // 1. Mark order EXPIRED
                    order.cancel("Quá thời hạn giữ chỗ 300 giây");
                    orderRepositoryPort.save(order);

                    // 2. Release Redis stock & user lock
                    if (redisLuaService != null) {
                        try {
                            Long itemId = Long.parseLong(order.getProductId());
                            redisLuaService.releaseReservation(1L, itemId, order.getUserId(), order.getQuantity());
                        } catch (Exception ex) {
                            log.warn("[ORPHAN RECONCILER] Không thể parse productId sang Long: {}", order.getProductId());
                        }
                    }

                    // 3. Emit OrderReservationExpiredEvent to Kafka topic order-events (Bảng 18)
                    String eventPayload = String.format("{\"orderId\":\"%s\",\"userId\":\"%s\",\"productId\":\"%s\",\"quantity\":%d,\"status\":\"EXPIRED\"}",
                            order.getOrderId(), order.getUserId(), order.getProductId(), order.getQuantity());

                    kafkaTemplate.send(KafkaTopicConstants.TOPIC_ORDER_EVENTS, order.getOrderId(), eventPayload);

                    if (outboxEventRepository != null) {
                        outboxEventRepository.save(OutboxEventEntity.builder()
                                .id(UUID.randomUUID().toString())
                                .aggregateType("ORDER")
                                .aggregateId(order.getOrderId())
                                .eventType("OrderReservationExpiredEvent")
                                .payload(eventPayload)
                                .status("SENT")
                                .retryCount(0)
                                .createdAt(Instant.now())
                                .build());
                    }

                    expiredCount++;
                    log.info("[ORPHAN RECONCILER] Đã tự động giải phóng giữ chỗ cho đơn hàng quá hạn [{}] của khách [{}]",
                            order.getOrderId(), order.getUserId());
                } catch (Exception ex) {
                    log.error("[ORPHAN RECONCILER ERROR] Lỗi giải phóng đơn mồ côi [{}]: {}", order.getOrderId(), ex.getMessage());
                }
            }
        }

        if (expiredCount > 0) {
            log.info("[ORPHAN RECONCILER SUMMARY] Hoàn tất đối soát, đã giải phóng {} đơn hàng mồ côi quá hạn 300s", expiredCount);
        }
    }
}
