package com.ecommerce.order.infrastructure.scheduler;

import com.ecommerce.common.config.KafkaTopicConstants;
import com.ecommerce.order.infrastructure.persistence.entity.OutboxEventEntity;
import com.ecommerce.order.infrastructure.persistence.repository.SpringDataOutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Mục 4.2.2 trong Báo cáo:
 * Tiến trình Outbox Scheduler quét định kỳ các bản ghi ở trạng thái PENDING
 * với chu kỳ fixedDelay = 200ms để thực hiện việc gửi lên Kafka Topic order-events.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxRelayScheduler {

    private final SpringDataOutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Scheduled(fixedDelay = 200)
    @Transactional
    public void relayOutboxEvents() {
        List<OutboxEventEntity> pendingEvents = outboxEventRepository.findTop50ByStatusOrderByCreatedAtAsc("PENDING");
        if (pendingEvents.isEmpty()) {
            return;
        }

        for (OutboxEventEntity event : pendingEvents) {
            try {
                String topic = KafkaTopicConstants.TOPIC_ORDER_EVENTS;
                kafkaTemplate.send(topic, event.getAggregateId(), event.getPayload()).get();
                event.setStatus("SENT");
                event.setSentAt(Instant.now());
                outboxEventRepository.save(event);
                log.info("[OUTBOX RELAY] Đã gửi thành công Outbox event [{}] aggregate [{}] sang topic [{}]",
                        event.getId(), event.getAggregateId(), topic);
            } catch (Exception ex) {
                event.setRetryCount(event.getRetryCount() + 1);
                if (event.getRetryCount() >= 5) {
                    event.setStatus("DEAD_LETTER");
                    log.error("[OUTBOX DEAD_LETTER] Outbox event [{}] vượt ngưỡng thử lại ({} lần): {}",
                            event.getId(), event.getRetryCount(), ex.getMessage());
                } else {
                    log.warn("[OUTBOX RETRY] Thất bại lần {} khi gửi outbox event [{}]: {}",
                            event.getRetryCount(), event.getId(), ex.getMessage());
                }
                outboxEventRepository.save(event);
            }
        }
    }
}
