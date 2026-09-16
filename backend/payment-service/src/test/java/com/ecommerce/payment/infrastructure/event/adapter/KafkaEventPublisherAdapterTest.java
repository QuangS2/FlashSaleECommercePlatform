package com.ecommerce.payment.infrastructure.event.adapter;

import com.ecommerce.common.config.KafkaTopicConstants;
import com.ecommerce.common.event.BaseEvent;
import com.ecommerce.common.kafka.EventPublisherService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KafkaEventPublisherAdapterTest {

    @Mock
    private EventPublisherService eventPublisherService;

    @InjectMocks
    private KafkaEventPublisherAdapter adapter;

    @Test
    @DisplayName("Test 1: publishPaymentCompletedEvent delegates to EventPublisherService")
    void testPublishPaymentCompletedEvent() {
        BaseEvent<?> event = BaseEvent.builder().eventId("ev-1").build();

        adapter.publishPaymentCompletedEvent("ORD-1", event);

        verify(eventPublisherService, times(1)).publish(eq(KafkaTopicConstants.TOPIC_PAYMENT_EVENTS), eq("ORD-1"), eq(event));
    }

    @Test
    @DisplayName("Test 2: publishPaymentFailedEvent delegates to EventPublisherService")
    void testPublishPaymentFailedEvent() {
        BaseEvent<?> event = BaseEvent.builder().eventId("ev-2").build();

        adapter.publishPaymentFailedEvent("ORD-2", event);

        verify(eventPublisherService, times(1)).publish(eq(KafkaTopicConstants.TOPIC_PAYMENT_EVENTS), eq("ORD-2"), eq(event));
    }

    @Test
    @DisplayName("Test 3: publishPaymentCompletedEvent with null event")
    void testPublishPaymentCompletedEventNull() {
        adapter.publishPaymentCompletedEvent("ORD-3", null);

        verify(eventPublisherService, times(1)).publish(eq(KafkaTopicConstants.TOPIC_PAYMENT_EVENTS), eq("ORD-3"), eq(null));
    }

    @Test
    @DisplayName("Test 4: publishPaymentFailedEvent with null orderId")
    void testPublishPaymentFailedEventNullOrderId() {
        BaseEvent<?> event = BaseEvent.builder().eventId("ev-4").build();

        adapter.publishPaymentFailedEvent(null, event);

        verify(eventPublisherService, times(1)).publish(eq(KafkaTopicConstants.TOPIC_PAYMENT_EVENTS), eq(null), eq(event));
    }

    @Test
    @DisplayName("Test 5: Verify adapter instantiation")
    void testAdapterInstantiation() {
        KafkaEventPublisherAdapter newAdapter = new KafkaEventPublisherAdapter(eventPublisherService);
        org.junit.jupiter.api.Assertions.assertNotNull(newAdapter);
    }
}
