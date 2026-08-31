package com.ecommerce.inventory.domain.port.out;

import com.ecommerce.common.event.BaseEvent;

/**
 * Outbound Port for Event Publisher.
 */
public interface EventPublisherPort {

    void publishInventoryReservedEvent(String orderId, BaseEvent<?> event);

    void publishInventoryReservationFailedEvent(String orderId, BaseEvent<?> event);

    void publishInventoryRestoredEvent(String orderId, BaseEvent<?> event);
}
