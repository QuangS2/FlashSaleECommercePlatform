package com.ecommerce.order.infrastructure.event.adapter;

import com.ecommerce.common.config.KafkaTopicConstants;
import com.ecommerce.common.event.BaseEvent;
import com.ecommerce.common.kafka.EventPublisherService;
import com.ecommerce.order.domain.port.out.EventPublisherPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaEventPublisherAdapter implements EventPublisherPort {

    private final EventPublisherService eventPublisherService;

    @Override
    public void publishOrderCreatedEvent(String orderId, BaseEvent<?> event) {
        eventPublisherService.publish(KafkaTopicConstants.TOPIC_ORDER_EVENTS, orderId, event);
    }

    @Override
    public void publishOrderConfirmedEvent(String orderId, BaseEvent<?> event) {
        eventPublisherService.publish(KafkaTopicConstants.TOPIC_ORDER_EVENTS, orderId, event);
    }

    @Override
    public void publishOrderCancelledEvent(String orderId, BaseEvent<?> event) {
        eventPublisherService.publish(KafkaTopicConstants.TOPIC_ORDER_EVENTS, orderId, event);
    }
}
