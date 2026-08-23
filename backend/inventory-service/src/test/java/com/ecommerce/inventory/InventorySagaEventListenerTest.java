package com.ecommerce.inventory;

import com.ecommerce.inventory.listener.InventorySagaEventListener;
import com.ecommerce.inventory.service.InventoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class InventorySagaEventListenerTest {

    @Mock
    private InventoryService inventoryService;

    @Mock
    private Acknowledgment acknowledgment;

    private InventorySagaEventListener listener;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        listener = new InventorySagaEventListener(inventoryService, objectMapper);
    }

    @Test
    @DisplayName("Test 1: onOrderEvent - ORDER_CREATED triggers reserveStock and commits ACK")
    public void testOnOrderEvent_OrderCreated() {
        String json = """
                {
                    "eventType": "ORDER_CREATED",
                    "payload": {
                        "orderId": "ORD-123",
                        "productId": "PROD-IPHONE",
                        "quantity": 2
                    }
                }
                """;

        listener.onOrderEvent(json, acknowledgment);

        verify(inventoryService).reserveStock("ORD-123", "PROD-IPHONE", 2);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Test 2: onOrderEvent - ORDER_CANCELLED triggers restoreStock (Compensating) and commits ACK")
    public void testOnOrderEvent_OrderCancelled() {
        String json = """
                {
                    "eventType": "ORDER_CANCELLED",
                    "payload": {
                        "orderId": "ORD-123",
                        "productId": "PROD-IPHONE",
                        "quantity": 2,
                        "reason": "Payment Failed"
                    }
                }
                """;

        listener.onOrderEvent(json, acknowledgment);

        verify(inventoryService).restoreStock("ORD-123", "PROD-IPHONE", 2, "Payment Failed");
        verify(acknowledgment).acknowledge();
    }
}
