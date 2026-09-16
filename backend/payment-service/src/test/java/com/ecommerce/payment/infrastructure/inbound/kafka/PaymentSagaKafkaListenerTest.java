package com.ecommerce.payment.infrastructure.inbound.kafka;

import com.ecommerce.common.event.inventory.InventoryReservedEvent;
import com.ecommerce.payment.application.port.in.PaymentUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentSagaKafkaListenerTest {

    @Mock
    private PaymentUseCase paymentUseCase;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Acknowledgment acknowledgment;

    @InjectMocks
    private PaymentSagaKafkaListener listener;

    @Test
    @DisplayName("Test 1: Successfully process INVENTORY_RESERVED event and trigger use case")
    void testOnInventoryEvent_InventoryReservedSuccess() {
        String json = "{\"eventType\":\"INVENTORY_RESERVED\",\"payload\":{\"orderId\":\"ORD-1\",\"userId\":\"user-1\",\"amount\":100.00}}";

        listener.onInventoryEvent(json, acknowledgment);

        verify(paymentUseCase, times(1)).handleInventoryReserved(any(InventoryReservedEvent.class));
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    @DisplayName("Test 2: Ignore non-INVENTORY_RESERVED events")
    void testOnInventoryEvent_OtherEventTypeIgnored() {
        String json = "{\"eventType\":\"INVENTORY_RESTORED\",\"payload\":{\"orderId\":\"ORD-2\"}}";

        listener.onInventoryEvent(json, acknowledgment);

        verify(paymentUseCase, never()).handleInventoryReserved(any());
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    @DisplayName("Test 3: Malformed JSON handled gracefully without exception leak")
    void testOnInventoryEvent_MalformedJson() {
        String malformed = "{invalid-json";

        listener.onInventoryEvent(malformed, acknowledgment);

        verify(paymentUseCase, never()).handleInventoryReserved(any());
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    @DisplayName("Test 4: Handle null Acknowledgment object safely")
    void testOnInventoryEvent_NullAcknowledgment() {
        String json = "{\"eventType\":\"INVENTORY_RESERVED\",\"payload\":{\"orderId\":\"ORD-4\",\"userId\":\"user-4\"}}";

        listener.onInventoryEvent(json, null);

        verify(paymentUseCase, times(1)).handleInventoryReserved(any());
    }

    @Test
    @DisplayName("Test 5: Handle exception thrown from paymentUseCase gracefully")
    void testOnInventoryEvent_UseCaseException() {
        String json = "{\"eventType\":\"INVENTORY_RESERVED\",\"payload\":{\"orderId\":\"ORD-5\"}}";
        doThrow(new RuntimeException("DB unavailable")).when(paymentUseCase).handleInventoryReserved(any());

        listener.onInventoryEvent(json, acknowledgment);

        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    @DisplayName("Test 6: Empty payload node handled gracefully")
    void testOnInventoryEvent_EmptyPayload() {
        String json = "{\"eventType\":\"INVENTORY_RESERVED\"}";

        listener.onInventoryEvent(json, acknowledgment);

        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    @DisplayName("Test 7: Missing eventType in json")
    void testOnInventoryEvent_MissingEventType() {
        String json = "{\"payload\":{\"orderId\":\"ORD-7\"}}";

        listener.onInventoryEvent(json, acknowledgment);

        verify(paymentUseCase, never()).handleInventoryReserved(any());
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    @DisplayName("Test 8: Null message handled safely")
    void testOnInventoryEvent_NullMessage() {
        listener.onInventoryEvent(null, acknowledgment);

        verify(paymentUseCase, never()).handleInventoryReserved(any());
        verify(acknowledgment, times(1)).acknowledge();
    }
}
