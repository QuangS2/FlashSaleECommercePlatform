package com.ecommerce.payment.listener;

import com.ecommerce.common.config.KafkaTopicConstants;
import com.ecommerce.common.event.EventType;
import com.ecommerce.common.event.inventory.InventoryReservedEvent;
import com.ecommerce.payment.service.PaymentService;
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
public class PaymentSagaEventListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentSagaEventListener.class);

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopicConstants.TOPIC_INVENTORY_EVENTS,
            groupId = KafkaTopicConstants.PAYMENT_SERVICE_GROUP,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onInventoryEvent(String message, Acknowledgment ack) {
        try {
            JsonNode root = objectMapper.readTree(message);
            String eventTypeStr = root.path("eventType").asText();
            JsonNode payloadNode = root.path("payload");

            if (EventType.INVENTORY_RESERVED.name().equals(eventTypeStr)) {
                InventoryReservedEvent event = objectMapper.treeToValue(payloadNode, InventoryReservedEvent.class);
                log.info("[PAYMENT SAGA LISTENER] Tiếp nhận sự kiện INVENTORY_RESERVED cho đơn hàng [{}] -> Kích hoạt thanh toán",
                        event.getOrderId());
                paymentService.handleInventoryReserved(event);
            }
        } catch (Exception e) {
            log.error("[PAYMENT LISTENER ERROR] Lỗi khi xử lý sự kiện từ topic inventory-events: {}", e.getMessage(), e);
        } finally {
            if (ack != null) {
                ack.acknowledge();
            }
        }
    }
}
