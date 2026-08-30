package com.ecommerce.inventory.infrastructure.event.adapter;

import com.ecommerce.common.config.KafkaTopicConstants;
import com.ecommerce.common.event.BaseEvent;
import com.ecommerce.common.kafka.EventPublisherService;
import com.ecommerce.inventory.domain.port.out.EventPublisherPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaEventPublisherAdapter implements EventPublisherPort {

    private final EventPublisherService eventPublisherService;

    @Override
    public void publishInventoryReservedEvent(String orderId, BaseEvent<?> event) {
        eventPublisherService.publish(KafkaTopicConstants.TOPIC_INVENTORY_EVENTS, orderId, event);
    }

    @Override
    public void publishInventoryReservationFailedEvent(String orderId, BaseEvent<?> event) {
        eventPublisherService.publish(KafkaTopicConstants.TOPIC_INVENTORY_EVENTS, orderId, event);
    }

    @Override
    public void publishInventoryRestoredEvent(String orderId, BaseEvent<?> event) {
        eventPublisherService.publish(KafkaTopicConstants.TOPIC_INVENTORY_EVENTS, orderId, event);
    }
}
