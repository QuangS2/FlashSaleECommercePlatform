package com.ecommerce.order;

import com.ecommerce.common.event.inventory.InventoryReservationFailedEvent;
import com.ecommerce.common.event.inventory.InventoryReservedEvent;
import com.ecommerce.common.event.payment.PaymentCompletedEvent;
import com.ecommerce.common.event.payment.PaymentFailedEvent;
import com.ecommerce.order.listener.OrderSagaEventListener;
import com.ecommerce.order.service.OrderService;
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
public class OrderSagaEventListenerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private Acknowledgment acknowledgment;

    private OrderSagaEventListener listener;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        listener = new OrderSagaEventListener(orderService, objectMapper);
    }

    @Test
    @DisplayName("Test 1: onInventoryEvent - INVENTORY_RESERVED triggers handleInventoryReserved and commits ACK")
    public void testOnInventoryEvent_Reserved() {
        String json = """
                {
                    "eventType": "INVENTORY_RESERVED",
                    "payload": {
                        "orderId": "ORD-999",
                        "productId": "PROD-1",
                        "quantityReserved": 1,
                        "remainingStock": 99
                    }
                }
                """;

        listener.onInventoryEvent(json, acknowledgment);

        verify(orderService).handleInventoryReserved(any(InventoryReservedEvent.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Test 2: onInventoryEvent - INVENTORY_RESERVATION_FAILED triggers handleInventoryReservationFailed")
    public void testOnInventoryEvent_Failed() {
        String json = """
                {
                    "eventType": "INVENTORY_RESERVATION_FAILED",
                    "payload": {
                        "orderId": "ORD-999",
                        "productId": "PROD-1",
                        "requestedQuantity": 5,
                        "failureReason": "Hết tồn kho"
                    }
                }
                """;

        listener.onInventoryEvent(json, acknowledgment);

        verify(orderService).handleInventoryReservationFailed(any(InventoryReservationFailedEvent.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Test 3: onPaymentEvent - PAYMENT_COMPLETED triggers handlePaymentCompleted")
    public void testOnPaymentEvent_Completed() {
        String json = """
                {
                    "eventType": "PAYMENT_COMPLETED",
                    "payload": {
                        "paymentId": "PAY-123",
                        "orderId": "ORD-999",
                        "amount": 250000.0,
                        "paymentMethod": "VNPAY"
                    }
                }
                """;

        listener.onPaymentEvent(json, acknowledgment);

        verify(orderService).handlePaymentCompleted(any(PaymentCompletedEvent.class));
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("Test 4: onPaymentEvent - PAYMENT_FAILED triggers handlePaymentFailed")
    public void testOnPaymentEvent_Failed() {
        String json = """
                {
                    "eventType": "PAYMENT_FAILED",
                    "payload": {
                        "orderId": "ORD-999",
                        "amount": 250000.0,
                        "failureReason": "Thẻ hết hạn"
                    }
                }
                """;

        listener.onPaymentEvent(json, acknowledgment);

        verify(orderService).handlePaymentFailed(any(PaymentFailedEvent.class));
        verify(acknowledgment).acknowledge();
    }
}
