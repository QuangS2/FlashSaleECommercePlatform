package com.ecommerce.order.infrastructure.inbound.rest;

import com.ecommerce.common.config.KafkaTopicConstants;
import com.ecommerce.order.infrastructure.persistence.entity.OutboxEventEntity;
import com.ecommerce.order.infrastructure.persistence.repository.SpringDataOutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Admin Controller for Outbox management (Bảng 17: POST /api/admin/outbox/replay).
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class AdminOutboxController {

    private final SpringDataOutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @PostMapping({"/api/admin/outbox/replay", "/api/v1/admin/outbox/replay"})
    public ResponseEntity<Map<String, Object>> replayOutboxEvents(
            @RequestParam(value = "aggregateType", defaultValue = "ORDER") String aggregateType,
            @RequestParam(value = "limit", defaultValue = "100") int limit
    ) {
        List<OutboxEventEntity> pendingEvents = outboxEventRepository.findTop50ByStatusOrderByCreatedAtAsc("PENDING");
        int count = 0;

        for (OutboxEventEntity event : pendingEvents) {
            try {
                String topic = KafkaTopicConstants.TOPIC_ORDER_EVENTS;
                kafkaTemplate.send(topic, event.getAggregateId(), event.getPayload());
                event.setStatus("SENT");
                event.setSentAt(Instant.now());
                outboxEventRepository.save(event);
                count++;
            } catch (Exception ex) {
                log.error("[OUTBOX REPLAY ERROR] Không thể gửi lại event [{}]: {}", event.getId(), ex.getMessage());
            }
        }

        log.info("[ADMIN OUTBOX REPLAY] Đã phát lại thành công {}/{} sự kiện Outbox", count, pendingEvents.size());
        return ResponseEntity.ok(Map.of(
                "replayedCount", count,
                "totalFound", pendingEvents.size(),
                "status", "SUCCESS",
                "message", "Đã gửi lại thành công " + count + " sự kiện Outbox"
        ));
    }
}
