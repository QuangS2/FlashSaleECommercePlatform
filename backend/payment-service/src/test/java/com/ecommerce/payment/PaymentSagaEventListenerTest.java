package com.ecommerce.payment;

import com.ecommerce.common.event.inventory.InventoryReservedEvent;
import com.ecommerce.payment.listener.PaymentSagaEventListener;
import com.ecommerce.payment.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class PaymentSagaEventListenerTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private Acknowledgment acknowledgment;

    private PaymentSagaEventListener listener;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        listener = new PaymentSagaEventListener(paymentService, objectMapper);
    }

    @Test
    @DisplayName("Test 1: onInventoryEvent - INVENTORY_RESERVED triggers handleInventoryReserved and commits ACK")
    public void testOnInventoryEvent_Reserved() {
        String json = """
                {
                    "eventType": "INVENTORY_RESERVED",
                    "payload": {
                        "orderId": "ORD-PAY-100",
                        "productId": "PROD-IPHONE-15",
                        "quantityReserved": 1,
                        "remainingStock": 99
                    }
                }
                """;

        listener.onInventoryEvent(json, acknowledgment);

        verify(paymentService).handleInventoryReserved(any(InventoryReservedEvent.class));
        verify(acknowledgment).acknowledge();
    }
}
