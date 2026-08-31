package com.ecommerce.payment.infrastructure.event.adapter;

import com.ecommerce.common.config.KafkaTopicConstants;
import com.ecommerce.common.event.BaseEvent;
import com.ecommerce.common.kafka.EventPublisherService;
import com.ecommerce.payment.domain.port.out.EventPublisherPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaEventPublisherAdapter implements EventPublisherPort {

    private final EventPublisherService eventPublisherService;

    @Override
    public void publishPaymentCompletedEvent(String orderId, BaseEvent<?> event) {
        eventPublisherService.publish(KafkaTopicConstants.TOPIC_PAYMENT_EVENTS, orderId, event);
    }

    @Override
    public void publishPaymentFailedEvent(String orderId, BaseEvent<?> event) {
        eventPublisherService.publish(KafkaTopicConstants.TOPIC_PAYMENT_EVENTS, orderId, event);
    }
}
