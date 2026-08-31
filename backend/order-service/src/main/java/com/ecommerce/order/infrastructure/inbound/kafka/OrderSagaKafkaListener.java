package com.ecommerce.order.infrastructure.inbound.kafka;

import com.ecommerce.common.config.KafkaTopicConstants;
import com.ecommerce.common.event.EventType;
import com.ecommerce.common.event.inventory.InventoryReservationFailedEvent;
import com.ecommerce.common.event.inventory.InventoryReservedEvent;
import com.ecommerce.common.event.payment.PaymentCompletedEvent;
import com.ecommerce.common.event.payment.PaymentFailedEvent;
import com.ecommerce.order.application.port.in.OrderUseCase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderSagaKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(OrderSagaKafkaListener.class);

    private final OrderUseCase orderUseCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopicConstants.TOPIC_INVENTORY_EVENTS,
            groupId = KafkaTopicConstants.ORDER_SERVICE_GROUP,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onInventoryEvent(String message, Acknowledgment ack) {
        try {
            JsonNode root = objectMapper.readTree(message);
            String eventTypeStr = root.path("eventType").asText();
            JsonNode payloadNode = root.path("payload");

            if (EventType.INVENTORY_RESERVED.name().equals(eventTypeStr)) {
                InventoryReservedEvent event = objectMapper.treeToValue(payloadNode, InventoryReservedEvent.class);
                orderUseCase.handleInventoryReserved(event);
            } else if (EventType.INVENTORY_RESERVATION_FAILED.name().equals(eventTypeStr)) {
                InventoryReservationFailedEvent event = objectMapper.treeToValue(payloadNode, InventoryReservationFailedEvent.class);
                orderUseCase.handleInventoryReservationFailed(event);
            }
        } catch (Exception e) {
            log.error("[ORDER LISTENER ERROR] Lỗi khi xử lý sự kiện từ topic inventory-events: {}", e.getMessage(), e);
        } finally {
            if (ack != null) {
                ack.acknowledge();
            }
        }
    }

    @KafkaListener(
            topics = KafkaTopicConstants.TOPIC_PAYMENT_EVENTS,
            groupId = KafkaTopicConstants.ORDER_SERVICE_GROUP,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onPaymentEvent(String message, Acknowledgment ack) {
        try {
            JsonNode root = objectMapper.readTree(message);
            String eventTypeStr = root.path("eventType").asText();
            JsonNode payloadNode = root.path("payload");

            if (EventType.PAYMENT_COMPLETED.name().equals(eventTypeStr)) {
                PaymentCompletedEvent event = objectMapper.treeToValue(payloadNode, PaymentCompletedEvent.class);
                orderUseCase.handlePaymentCompleted(event);
            } else if (EventType.PAYMENT_FAILED.name().equals(eventTypeStr)) {
                PaymentFailedEvent event = objectMapper.treeToValue(payloadNode, PaymentFailedEvent.class);
                orderUseCase.handlePaymentFailed(event);
            }
        } catch (Exception e) {
            log.error("[ORDER LISTENER ERROR] Lỗi khi xử lý sự kiện từ topic payment-events: {}", e.getMessage(), e);
        } finally {
            if (ack != null) {
                ack.acknowledge();
            }
        }
    }
}
