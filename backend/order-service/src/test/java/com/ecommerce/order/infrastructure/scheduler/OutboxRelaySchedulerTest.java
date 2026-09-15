package com.ecommerce.order.infrastructure.scheduler;

import com.ecommerce.order.infrastructure.persistence.entity.OutboxEventEntity;
import com.ecommerce.order.infrastructure.persistence.repository.SpringDataOutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxRelaySchedulerTest {

    @Mock
    private SpringDataOutboxEventRepository outboxEventRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private OutboxRelayScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new OutboxRelayScheduler(outboxEventRepository, kafkaTemplate);
    }

    @Test
    @DisplayName("Test 1: Relay does nothing when there are no pending events")
    void testRelayNoEvents() {
        when(outboxEventRepository.findTop50ByStatusOrderByCreatedAtAsc("PENDING"))
                .thenReturn(Collections.emptyList());

        scheduler.relayOutboxEvents();

        verifyNoInteractions(kafkaTemplate);
        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    @DisplayName("Test 2: Relay sends pending event to Kafka and marks SENT")
    void testRelaySuccess() {
        OutboxEventEntity event = OutboxEventEntity.builder()
                .id("outbox-1")
                .aggregateType("ORDER")
                .aggregateId("ord-100")
                .eventType("OrderCreatedEvent")
                .payload("{\"orderId\":\"ord-100\"}")
                .status("PENDING")
                .retryCount(0)
                .createdAt(Instant.now())
                .build();

        when(outboxEventRepository.findTop50ByStatusOrderByCreatedAtAsc("PENDING"))
                .thenReturn(List.of(event));

        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        future.complete(mock(SendResult.class));
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

        scheduler.relayOutboxEvents();

        ArgumentCaptor<OutboxEventEntity> captor = ArgumentCaptor.forClass(OutboxEventEntity.class);
        verify(outboxEventRepository).save(captor.capture());

        OutboxEventEntity saved = captor.getValue();
        assertEquals("SENT", saved.getStatus());
        assertNotNull(saved.getSentAt());
    }

    @Test
    @DisplayName("Test 3: Relay failure increments retry count and keeps status PENDING")
    void testRelayFailureIncrementsRetry() {
        OutboxEventEntity event = OutboxEventEntity.builder()
                .id("outbox-err")
                .aggregateType("ORDER")
                .aggregateId("ord-err")
                .eventType("OrderCreatedEvent")
                .payload("{\"orderId\":\"ord-err\"}")
                .status("PENDING")
                .retryCount(0)
                .createdAt(Instant.now())
                .build();

        when(outboxEventRepository.findTop50ByStatusOrderByCreatedAtAsc("PENDING"))
                .thenReturn(List.of(event));

        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Kafka Broker Unavailable"));
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

        scheduler.relayOutboxEvents();

        ArgumentCaptor<OutboxEventEntity> captor = ArgumentCaptor.forClass(OutboxEventEntity.class);
        verify(outboxEventRepository).save(captor.capture());

        OutboxEventEntity saved = captor.getValue();
        assertEquals(1, saved.getRetryCount());
        assertEquals("PENDING", saved.getStatus());
    }

    @Test
    @DisplayName("Test 4: Relay failure marks DEAD_LETTER when retry reaches 5")
    void testRelayMaxRetryMarksDeadLetter() {
        OutboxEventEntity event = OutboxEventEntity.builder()
                .id("outbox-dead")
                .aggregateType("ORDER")
                .aggregateId("ord-dead")
                .eventType("OrderCreatedEvent")
                .payload("{\"orderId\":\"ord-dead\"}")
                .status("PENDING")
                .retryCount(4)
                .createdAt(Instant.now())
                .build();

        when(outboxEventRepository.findTop50ByStatusOrderByCreatedAtAsc("PENDING"))
                .thenReturn(List.of(event));

        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Permanent Cluster Failure"));
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

        scheduler.relayOutboxEvents();

        ArgumentCaptor<OutboxEventEntity> captor = ArgumentCaptor.forClass(OutboxEventEntity.class);
        verify(outboxEventRepository).save(captor.capture());

        OutboxEventEntity saved = captor.getValue();
        assertEquals(5, saved.getRetryCount());
        assertEquals("DEAD_LETTER", saved.getStatus());
    }
}
