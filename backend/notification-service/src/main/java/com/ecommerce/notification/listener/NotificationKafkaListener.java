package com.ecommerce.notification.listener;

import com.ecommerce.common.config.KafkaTopicConstants;
import com.ecommerce.common.event.EventType;
import com.ecommerce.common.event.inventory.InventoryReservationFailedEvent;
import com.ecommerce.common.event.inventory.InventoryReservedEvent;
import com.ecommerce.common.event.inventory.InventoryRestoredEvent;
import com.ecommerce.common.event.order.OrderCancelledEvent;
import com.ecommerce.common.event.order.OrderConfirmedEvent;
import com.ecommerce.common.event.order.OrderCreatedEvent;
import com.ecommerce.common.event.payment.PaymentCompletedEvent;
import com.ecommerce.common.event.payment.PaymentFailedEvent;
import com.ecommerce.notification.dto.NotificationMessage;
import com.ecommerce.notification.service.NotificationService;
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
public class NotificationKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationKafkaListener.class);

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = {
                    KafkaTopicConstants.TOPIC_ORDER_EVENTS,
                    KafkaTopicConstants.TOPIC_INVENTORY_EVENTS,
                    KafkaTopicConstants.TOPIC_PAYMENT_EVENTS,
                    KafkaTopicConstants.TOPIC_NOTIFICATION_EVENTS
            },
            groupId = KafkaTopicConstants.NOTIFICATION_SERVICE_GROUP,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onEvent(String message, Acknowledgment ack) {
        try {
            JsonNode root = objectMapper.readTree(message);
            String eventTypeStr = root.path("eventType").asText();
            JsonNode payloadNode = root.path("payload");

            if (EventType.ORDER_CREATED.name().equals(eventTypeStr)) {
                OrderCreatedEvent event = objectMapper.treeToValue(payloadNode, OrderCreatedEvent.class);
                NotificationMessage notif = NotificationMessage.of(
                        event.getUserId(),
                        event.getOrderId(),
                        "ORDER_CREATED",
                        "Đã nhận đơn hàng Flash Sale",
                        "Đơn hàng #" + event.getOrderId() + " (" + event.getProductTitle() + ") đã được tiếp nhận và đang giữ chỗ kho."
                );
                notificationService.sendNotificationToUser(event.getUserId(), notif);
                notificationService.broadcastOrderUpdate(event.getOrderId(), notif);

            } else if (EventType.ORDER_CONFIRMED.name().equals(eventTypeStr)) {
                OrderConfirmedEvent event = objectMapper.treeToValue(payloadNode, OrderConfirmedEvent.class);
                NotificationMessage notif = NotificationMessage.of(
                        event.getUserId(),
                        event.getOrderId(),
                        "ORDER_CONFIRMED",
                        "Đặt hàng thành công!",
                        "Chúc mừng quý khách! Đơn hàng #" + event.getOrderId() + " đã được xác nhận thành công 100%."
                );
                notificationService.sendNotificationToUser(event.getUserId(), notif);
                notificationService.broadcastOrderUpdate(event.getOrderId(), notif);

            } else if (EventType.ORDER_CANCELLED.name().equals(eventTypeStr)) {
                OrderCancelledEvent event = objectMapper.treeToValue(payloadNode, OrderCancelledEvent.class);
                NotificationMessage notif = NotificationMessage.of(
                        event.getUserId(),
                        event.getOrderId(),
                        "ORDER_CANCELLED",
                        "Đơn hàng đã bị hủy",
                        "Đơn hàng #" + event.getOrderId() + " đã bị hủy. Lý do: " + event.getReason()
                );
                notificationService.sendNotificationToUser(event.getUserId(), notif);
                notificationService.broadcastOrderUpdate(event.getOrderId(), notif);

            } else if (EventType.INVENTORY_RESERVED.name().equals(eventTypeStr)) {
                InventoryReservedEvent event = objectMapper.treeToValue(payloadNode, InventoryReservedEvent.class);
                notificationService.broadcastStockUpdate(event.getProductId(), event.getRemainingStock(), "RESERVED");

            } else if (EventType.INVENTORY_RESTORED.name().equals(eventTypeStr)) {
                InventoryRestoredEvent event = objectMapper.treeToValue(payloadNode, InventoryRestoredEvent.class);
                notificationService.broadcastStockUpdate(event.getProductId(), event.getUpdatedStock(), "RESTORED");

            } else if (EventType.INVENTORY_RESERVATION_FAILED.name().equals(eventTypeStr)) {
                InventoryReservationFailedEvent event = objectMapper.treeToValue(payloadNode, InventoryReservationFailedEvent.class);
                NotificationMessage notif = NotificationMessage.of(
                        "broadcast-user",
                        event.getOrderId(),
                        "OUT_OF_STOCK",
                        "Hết hàng Flash Sale",
                        "Rất tiếc! Sản phẩm trong đơn hàng #" + event.getOrderId() + " đã hết số lượng tồn kho Flash Sale."
                );
                notificationService.broadcastOrderUpdate(event.getOrderId(), notif);

            } else if (EventType.PAYMENT_COMPLETED.name().equals(eventTypeStr)) {
                PaymentCompletedEvent event = objectMapper.treeToValue(payloadNode, PaymentCompletedEvent.class);
                NotificationMessage notif = NotificationMessage.of(
                        event.getUserId(),
                        event.getOrderId(),
                        "PAYMENT_SUCCESS",
                        "Thanh toán thành công",
                        "Đã thanh toán thành công số tiền " + event.getAmount() + " VNĐ qua cổng " + event.getPaymentMethod()
                );
                notificationService.sendNotificationToUser(event.getUserId(), notif);
                notificationService.broadcastOrderUpdate(event.getOrderId(), notif);

            } else if (EventType.PAYMENT_FAILED.name().equals(eventTypeStr)) {
                PaymentFailedEvent event = objectMapper.treeToValue(payloadNode, PaymentFailedEvent.class);
                NotificationMessage notif = NotificationMessage.of(
                        event.getUserId(),
                        event.getOrderId(),
                        "PAYMENT_FAILED",
                        "Thanh toán không thành công",
                        "Thanh toán cho đơn hàng #" + event.getOrderId() + " không thành công. Lý do: " + event.getFailureReason()
                );
                notificationService.sendNotificationToUser(event.getUserId(), notif);
                notificationService.broadcastOrderUpdate(event.getOrderId(), notif);
            }
        } catch (Exception e) {
            log.error("[NOTIFICATION LISTENER ERROR] Lỗi xử lý sự kiện Kafka: {}", e.getMessage(), e);
        } finally {
            if (ack != null) {
                ack.acknowledge();
            }
        }
    }
}
