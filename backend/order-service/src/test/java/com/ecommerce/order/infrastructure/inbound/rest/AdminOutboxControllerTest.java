package com.ecommerce.order.infrastructure.inbound.rest;

import com.ecommerce.order.infrastructure.persistence.entity.OutboxEventEntity;
import com.ecommerce.order.infrastructure.persistence.repository.SpringDataOutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminOutboxControllerTest {

    @Mock
    private SpringDataOutboxEventRepository outboxEventRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private AdminOutboxController adminOutboxController;

    @Test
    void testReplayOutboxEventsSuccess() {
        OutboxEventEntity event = OutboxEventEntity.builder()
                .id("ev-1")
                .aggregateId("ord-1")
                .payload("{\"orderId\":\"ord-1\"}")
                .status("PENDING")
                .createdAt(Instant.now())
                .build();

        when(outboxEventRepository.findTop50ByStatusOrderByCreatedAtAsc("PENDING")).thenReturn(List.of(event));

        ResponseEntity<Map<String, Object>> response = adminOutboxController.replayOutboxEvents("ORDER", 50);

        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().get("replayedCount"));
        assertEquals("SUCCESS", response.getBody().get("status"));
        verify(kafkaTemplate, times(1)).send(anyString(), eq("ord-1"), any());
        verify(outboxEventRepository, times(1)).save(event);
        assertEquals("SENT", event.getStatus());
    }

    @Test
    void testReplayOutboxEventsEmpty() {
        when(outboxEventRepository.findTop50ByStatusOrderByCreatedAtAsc("PENDING")).thenReturn(List.of());

        ResponseEntity<Map<String, Object>> response = adminOutboxController.replayOutboxEvents("ORDER", 50);

        assertEquals(0, response.getBody().get("replayedCount"));
        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void testReplayOutboxEvents_ExceptionHandling() {
        OutboxEventEntity event = OutboxEventEntity.builder()
                .id("ev-err")
                .aggregateId("ord-err")
                .payload("{\"orderId\":\"ord-err\"}")
                .status("PENDING")
                .createdAt(Instant.now())
                .build();

        when(outboxEventRepository.findTop50ByStatusOrderByCreatedAtAsc("PENDING")).thenReturn(List.of(event));
        doThrow(new RuntimeException("Kafka unreachable")).when(kafkaTemplate).send(anyString(), anyString(), any());

        ResponseEntity<Map<String, Object>> response = adminOutboxController.replayOutboxEvents("ORDER", 50);

        assertEquals(0, response.getBody().get("replayedCount"));
        verify(outboxEventRepository, never()).save(event);
    }
}
