package com.ecommerce.payment.domain.port.out;

import com.ecommerce.common.event.BaseEvent;

/**
 * Outbound Port for Event Publisher.
 */
public interface EventPublisherPort {

    void publishPaymentCompletedEvent(String orderId, BaseEvent<?> event);

    void publishPaymentFailedEvent(String orderId, BaseEvent<?> event);
}
