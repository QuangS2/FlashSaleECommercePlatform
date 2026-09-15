package com.ecommerce.common.kafka;

import com.ecommerce.common.event.BaseEvent;
import com.ecommerce.common.event.EventType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventPublisherServiceTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private EventPublisherService eventPublisherService;

    @BeforeEach
    void setUp() {
        lenient().when(kafkaTemplate.send(anyString(), nullable(String.class), any()))
                .thenReturn(new CompletableFuture<>());
    }

    @Test
    @DisplayName("Test 1: Successfully publish BaseEvent with key to Kafka")
    void testPublishSuccess() {
        BaseEvent<String> event = BaseEvent.of(EventType.ORDER_CREATED, "corr-1", "order-service", "payload");

        CompletableFuture<?> future = eventPublisherService.publish("topic-test", "key-1", event);

        assertNotNull(future);
        verify(kafkaTemplate, times(1)).send(eq("topic-test"), eq("key-1"), eq(event));
    }

    @Test
    @DisplayName("Test 2: Publish BaseEvent without key uses correlationId as default key")
    void testPublishWithoutKey() {
        BaseEvent<String> event = BaseEvent.of(EventType.INVENTORY_RESERVED, "corr-2", "inventory-service", "payload-2");

        CompletableFuture<?> future = eventPublisherService.publish("topic-auto-key", event);

        assertNotNull(future);
        verify(kafkaTemplate, times(1)).send(eq("topic-auto-key"), eq("corr-2"), eq(event));
    }

    @Test
    @DisplayName("Test 3: Publish with null key delegates to KafkaTemplate with null key")
    void testPublishNullKey() {
        BaseEvent<String> event = BaseEvent.of(EventType.PAYMENT_COMPLETED, null, "payment-service", "payload-3");

        CompletableFuture<?> future = eventPublisherService.publish("topic-null-key", null, event);

        assertNotNull(future);
        verify(kafkaTemplate, times(1)).send(eq("topic-null-key"), isNull(), eq(event));
    }

    @Test
    @DisplayName("Test 4: Handle KafkaTemplate send failure when future completes exceptionally")
    void testPublishKafkaFailure() {
        CompletableFuture<org.springframework.kafka.support.SendResult<String, Object>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Kafka broker down"));
        when(kafkaTemplate.send(anyString(), nullable(String.class), any())).thenReturn(failedFuture);

        BaseEvent<String> event = BaseEvent.of(EventType.ORDER_CANCELLED, "corr-fail", "order-service", "payload-fail");

        CompletableFuture<?> future = eventPublisherService.publish("topic-fail", "key-fail", event);

        assertNotNull(future);
        assertTrue(future.isCompletedExceptionally());
    }

    @Test
    @DisplayName("Test 5: Publish without key and without correlationId falls back to eventId")
    void testPublishFallbackToEventId() {
        BaseEvent<String> event = BaseEvent.<String>builder()
                .eventId("ev-fallback-id")
                .eventType(EventType.ORDER_CREATED)
                .correlationId(null)
                .payload("payload")
                .build();

        CompletableFuture<?> future = eventPublisherService.publish("topic-fallback", event);

        assertNotNull(future);
        verify(kafkaTemplate, times(1)).send(eq("topic-fallback"), eq("ev-fallback-id"), eq(event));
    }
}
