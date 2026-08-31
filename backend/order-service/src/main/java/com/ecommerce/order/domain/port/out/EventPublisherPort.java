package com.ecommerce.order.domain.port.out;

import com.ecommerce.common.event.BaseEvent;

/**
 * Outbound Port for Event Publisher.
 * This abstracts away Kafka or any other messaging infrastructure.
 */
public interface EventPublisherPort {

    void publishOrderCreatedEvent(String orderId, BaseEvent<?> event);

    void publishOrderConfirmedEvent(String orderId, BaseEvent<?> event);

    void publishOrderCancelledEvent(String orderId, BaseEvent<?> event);
}
