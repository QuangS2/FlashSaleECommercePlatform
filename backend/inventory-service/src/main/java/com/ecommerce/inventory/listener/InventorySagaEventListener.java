package com.ecommerce.inventory.listener;

import com.ecommerce.common.config.KafkaTopicConstants;
import com.ecommerce.common.event.EventType;
import com.ecommerce.common.event.order.OrderCancelledEvent;
import com.ecommerce.common.event.order.OrderCreatedEvent;
import com.ecommerce.inventory.service.InventoryService;
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
public class InventorySagaEventListener {

    private static final Logger log = LoggerFactory.getLogger(InventorySagaEventListener.class);

    private final InventoryService inventoryService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = KafkaTopicConstants.TOPIC_ORDER_EVENTS,
            groupId = KafkaTopicConstants.INVENTORY_SERVICE_GROUP,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onOrderEvent(String message, Acknowledgment ack) {
        try {
            JsonNode root = objectMapper.readTree(message);
            String eventTypeStr = root.path("eventType").asText();
            JsonNode payloadNode = root.path("payload");

            if (EventType.ORDER_CREATED.name().equals(eventTypeStr)) {
                OrderCreatedEvent event = objectMapper.treeToValue(payloadNode, OrderCreatedEvent.class);
                log.info("[INVENTORY SAGA LISTENER] Tiếp nhận sự kiện ORDER_CREATED cho đơn hàng [{}] (Sản phẩm: {}, Số lượng: {})",
                        event.getOrderId(), event.getProductId(), event.getQuantity());
                inventoryService.reserveStock(event.getOrderId(), event.getProductId(), event.getQuantity());
            } else if (EventType.ORDER_CANCELLED.name().equals(eventTypeStr)) {
                OrderCancelledEvent event = objectMapper.treeToValue(payloadNode, OrderCancelledEvent.class);
                log.info("[INVENTORY SAGA LISTENER] Tiếp nhận sự kiện bù trừ ORDER_CANCELLED cho đơn hàng [{}] (Hoàn lại {} sản phẩm [{}])",
                        event.getOrderId(), event.getQuantity(), event.getProductId());
                inventoryService.restoreStock(event.getOrderId(), event.getProductId(), event.getQuantity(), event.getReason());
            }
        } catch (Exception e) {
            log.error("[INVENTORY LISTENER ERROR] Lỗi xử lý sự kiện từ topic order-events: {}", e.getMessage(), e);
        } finally {
            if (ack != null) {
                ack.acknowledge();
            }
        }
    }
}
