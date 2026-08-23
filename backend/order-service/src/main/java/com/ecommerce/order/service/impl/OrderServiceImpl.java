package com.ecommerce.order.service.impl;

import com.ecommerce.common.config.KafkaTopicConstants;
import com.ecommerce.common.event.BaseEvent;
import com.ecommerce.common.event.EventType;
import com.ecommerce.common.event.inventory.InventoryReservationFailedEvent;
import com.ecommerce.common.event.inventory.InventoryReservedEvent;
import com.ecommerce.common.event.order.OrderCancelledEvent;
import com.ecommerce.common.event.order.OrderConfirmedEvent;
import com.ecommerce.common.event.order.OrderCreatedEvent;
import com.ecommerce.common.event.order.OrderStatus;
import com.ecommerce.common.event.payment.PaymentCompletedEvent;
import com.ecommerce.common.event.payment.PaymentFailedEvent;
import com.ecommerce.common.kafka.EventPublisherService;
import com.ecommerce.order.dto.CreateOrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final EventPublisherService eventPublisherService;

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase() + "-" + System.currentTimeMillis();
        BigDecimal totalAmount = request.getUnitPrice().multiply(BigDecimal.valueOf(request.getQuantity()));

        Order order = Order.builder()
                .orderId(orderId)
                .userId(request.getUserId())
                .userEmail(request.getUserEmail())
                .productId(request.getProductId())
                .productTitle(request.getProductTitle() != null ? request.getProductTitle() : "Flash Sale Product")
                .quantity(request.getQuantity())
                .unitPrice(request.getUnitPrice())
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING)
                .build();

        Order savedOrder = orderRepository.save(order);
        log.info("[ORDER SERVICE] Đã khởi tạo đơn hàng ban đầu [{}] trạng thái [PENDING]", orderId);

        // Phát sự kiện OrderCreatedEvent lên Topic order-events
        OrderCreatedEvent payload = OrderCreatedEvent.builder()
                .orderId(savedOrder.getOrderId())
                .userId(savedOrder.getUserId())
                .userEmail(savedOrder.getUserEmail())
                .productId(savedOrder.getProductId())
                .productTitle(savedOrder.getProductTitle())
                .quantity(savedOrder.getQuantity())
                .unitPrice(savedOrder.getUnitPrice())
                .totalAmount(savedOrder.getTotalAmount())
                .status(savedOrder.getStatus())
                .createdAt(savedOrder.getCreatedAt())
                .build();

        BaseEvent<OrderCreatedEvent> event = BaseEvent.of(
                EventType.ORDER_CREATED,
                "CORR-" + savedOrder.getOrderId(),
                "order-service",
                payload
        );

        eventPublisherService.publish(KafkaTopicConstants.TOPIC_ORDER_EVENTS, savedOrder.getOrderId(), event);

        return OrderResponse.fromEntity(savedOrder, "Đơn hàng đã được tiếp nhận và đang được điều phối qua Saga Choreography.");
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByOrderId(String orderId) {
        return orderRepository.findByOrderId(orderId)
                .map(order -> OrderResponse.fromEntity(order, "Truy vấn trạng thái đơn hàng thành công."))
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng với mã: " + orderId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(String userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(order -> OrderResponse.fromEntity(order, null))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void handleInventoryReserved(InventoryReservedEvent event) {
        log.info("[SAGA CHOREOGRAPHY] Nhận sự kiện INVENTORY_RESERVED cho đơn hàng [{}]", event.getOrderId());
        orderRepository.findByOrderId(event.getOrderId()).ifPresent(order -> {
            if (order.getStatus() == OrderStatus.PENDING) {
                order.setStatus(OrderStatus.INVENTORY_RESERVED);
                orderRepository.save(order);
                log.info("[ORDER SERVICE] Cập nhật đơn hàng [{}] thành [INVENTORY_RESERVED]", order.getOrderId());
            }
        });
    }

    @Override
    @Transactional
    public void handleInventoryReservationFailed(InventoryReservationFailedEvent event) {
        log.warn("[SAGA CHOREOGRAPHY] Nhận sự kiện INVENTORY_RESERVATION_FAILED cho đơn hàng [{}], Lý do: {}",
                event.getOrderId(), event.getFailureReason());
        orderRepository.findByOrderId(event.getOrderId()).ifPresent(order -> {
            order.setStatus(OrderStatus.CANCELLED_OUT_OF_STOCK);
            order.setCancelReason(event.getFailureReason());
            orderRepository.save(order);
            log.info("[ORDER SERVICE] Đã huỷ đơn hàng [{}] do hết hàng Flash Sale", order.getOrderId());
        });
    }

    @Override
    @Transactional
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("[SAGA CHOREOGRAPHY] Nhận sự kiện PAYMENT_COMPLETED cho đơn hàng [{}], PaymentId: {}",
                event.getOrderId(), event.getPaymentId());
        orderRepository.findByOrderId(event.getOrderId()).ifPresent(order -> {
            order.setStatus(OrderStatus.CONFIRMED);
            order.setPaymentId(event.getPaymentId());
            Order updatedOrder = orderRepository.save(order);
            log.info("[ORDER SERVICE] Đơn hàng [{}] đã được [CONFIRMED] thành công 100%!", order.getOrderId());

            // Phát OrderConfirmedEvent lên Kafka để notification-service gửi thông báo
            OrderConfirmedEvent confirmedPayload = OrderConfirmedEvent.builder()
                    .orderId(updatedOrder.getOrderId())
                    .paymentId(updatedOrder.getPaymentId())
                    .totalAmount(updatedOrder.getTotalAmount())
                    .confirmedAt(updatedOrder.getUpdatedAt())
                    .build();

            BaseEvent<OrderConfirmedEvent> confirmedEvent = BaseEvent.of(
                    EventType.ORDER_CONFIRMED,
                    "CORR-" + updatedOrder.getOrderId(),
                    "order-service",
                    confirmedPayload
            );

            eventPublisherService.publish(KafkaTopicConstants.TOPIC_ORDER_EVENTS, updatedOrder.getOrderId(), confirmedEvent);
        });
    }

    @Override
    @Transactional
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.error("[SAGA CHOREOGRAPHY] Nhận sự kiện PAYMENT_FAILED cho đơn hàng [{}], Lý do: {}",
                event.getOrderId(), event.getFailureReason());
        orderRepository.findByOrderId(event.getOrderId()).ifPresent(order -> {
            order.setStatus(OrderStatus.PAYMENT_FAILED);
            order.setCancelReason(event.getFailureReason());
            Order updatedOrder = orderRepository.save(order);
            log.info("[ORDER SERVICE] Đã cập nhật đơn hàng [{}] thành [PAYMENT_FAILED]", order.getOrderId());

            // Phát OrderCancelledEvent (Compensating Transaction) để inventory-service hoàn trả lại kho
            OrderCancelledEvent cancelledPayload = OrderCancelledEvent.builder()
                    .orderId(updatedOrder.getOrderId())
                    .productId(updatedOrder.getProductId())
                    .quantity(updatedOrder.getQuantity())
                    .reason(event.getFailureReason())
                    .cancelledAt(updatedOrder.getUpdatedAt())
                    .build();

            BaseEvent<OrderCancelledEvent> cancelledEvent = BaseEvent.of(
                    EventType.ORDER_CANCELLED,
                    "CORR-" + updatedOrder.getOrderId(),
                    "order-service",
                    cancelledPayload
            );

            eventPublisherService.publish(KafkaTopicConstants.TOPIC_ORDER_EVENTS, updatedOrder.getOrderId(), cancelledEvent);
        });
    }
}
