package com.ecommerce.common.kafka;

import com.ecommerce.common.event.BaseEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class EventPublisherService {

    private static final Logger log = LoggerFactory.getLogger(EventPublisherService.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public CompletableFuture<SendResult<String, Object>> publish(String topic, String key, BaseEvent<?> event) {
        log.info("[KAFKA PUBLISH] Gửi sự kiện [{}] (ID: {}, Key: {}) tới Topic [{}]",
                event.getEventType(), event.getEventId(), key, topic);

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("[KAFKA ERROR] Thất bại khi gửi sự kiện [{}] (ID: {}) tới Topic [{}]: {}",
                        event.getEventType(), event.getEventId(), topic, ex.getMessage(), ex);
            } else if (result != null && result.getRecordMetadata() != null) {
                log.info("[KAFKA SUCCESS] Đã gửi thành công sự kiện [{}] tới Topic [{}] - Partition: {}, Offset: {}",
                        event.getEventType(),
                        topic,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });

        return future;
    }

    public CompletableFuture<SendResult<String, Object>> publish(String topic, BaseEvent<?> event) {
        String key = event.getCorrelationId() != null ? event.getCorrelationId() : event.getEventId();
        return publish(topic, key, event);
    }
}
