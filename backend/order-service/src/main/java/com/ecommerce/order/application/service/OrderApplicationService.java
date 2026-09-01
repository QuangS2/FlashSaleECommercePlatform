package com.ecommerce.order.application.service;

import com.ecommerce.common.event.BaseEvent;
import com.ecommerce.common.event.EventType;
import com.ecommerce.common.event.inventory.InventoryReservationFailedEvent;
import com.ecommerce.common.event.inventory.InventoryReservedEvent;
import com.ecommerce.common.event.order.OrderCancelledEvent;
import com.ecommerce.common.event.order.OrderConfirmedEvent;
import com.ecommerce.common.event.order.OrderCreatedEvent;
import com.ecommerce.common.event.payment.PaymentCompletedEvent;
import com.ecommerce.common.event.payment.PaymentFailedEvent;
import com.ecommerce.order.application.port.in.OrderUseCase;
import com.ecommerce.order.domain.entity.Order;
import com.ecommerce.order.domain.port.out.EventPublisherPort;
import com.ecommerce.order.domain.port.out.OrderRepositoryPort;
import com.ecommerce.order.dto.CreateOrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Application Service.
 * Orchestrates use cases using the Domain Entity and Outbound Ports.
 */
@Service
@RequiredArgsConstructor
public class OrderApplicationService implements OrderUseCase {

    private static final Logger log = LoggerFactory.getLogger(OrderApplicationService.class);

    private final OrderRepositoryPort orderRepositoryPort;
    private final EventPublisherPort eventPublisherPort;

    @Override
    public OrderResponse createOrder(CreateOrderRequest request) {
        // Delegate to Domain Entity factory method
        Order newOrder = Order.createNew(
                request.getUserId(),
                request.getUserEmail(),
                request.getProductId(),
                request.getProductTitle(),
                request.getQuantity(),
                request.getUnitPrice()
        );

        Order savedOrder = orderRepositoryPort.save(newOrder);
        log.info("[ORDER SERVICE] Đã khởi tạo đơn hàng ban đầu [{}] trạng thái [PENDING]", savedOrder.getOrderId());

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

        eventPublisherPort.publishOrderCreatedEvent(savedOrder.getOrderId(), event);

        return OrderResponse.fromEntity(savedOrder, "Đơn hàng đã được tiếp nhận và đang được điều phối qua Saga Choreography.");
    }

    @Override
    public OrderResponse getOrderByOrderId(String orderId) {
        return orderRepositoryPort.findByOrderId(orderId)
                .map(order -> OrderResponse.fromEntity(order, "Truy vấn trạng thái đơn hàng thành công."))
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng với mã: " + orderId));
    }

    @Override
    public List<OrderResponse> getOrdersByUserId(String userId) {
        return orderRepositoryPort.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(order -> OrderResponse.fromEntity(order, null))
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderResponse> getOrdersByUserEmail(String userEmail) {
        return orderRepositoryPort.findByUserEmailOrderByCreatedAtDesc(userEmail)
                .stream()
                .map(order -> OrderResponse.fromEntity(order, null))
                .collect(Collectors.toList());
    }

    @Override
    public void handleInventoryReserved(InventoryReservedEvent event) {
        log.info("[SAGA CHOREOGRAPHY] Nhận sự kiện INVENTORY_RESERVED cho đơn hàng [{}]", event.getOrderId());
        orderRepositoryPort.findByOrderId(event.getOrderId()).ifPresent(order -> {
            try {
                order.markInventoryReserved(); // Domain logic
                orderRepositoryPort.save(order);
                log.info("[ORDER SERVICE] Cập nhật đơn hàng [{}] thành [INVENTORY_RESERVED]", order.getOrderId());
            } catch (Exception e) {
                log.warn("[ORDER SERVICE] Bỏ qua sự kiện do trạng thái không hợp lệ: {}", e.getMessage());
            }
        });
    }

    @Override
    public void handleInventoryReservationFailed(InventoryReservationFailedEvent event) {
        log.warn("[SAGA CHOREOGRAPHY] Nhận sự kiện INVENTORY_RESERVATION_FAILED cho đơn hàng [{}], Lý do: {}",
                event.getOrderId(), event.getFailureReason());
        orderRepositoryPort.findByOrderId(event.getOrderId()).ifPresent(order -> {
            order.markInventoryReservationFailed(event.getFailureReason()); // Domain logic
            orderRepositoryPort.save(order);
            log.info("[ORDER SERVICE] Đã huỷ đơn hàng [{}] do hết hàng Flash Sale", order.getOrderId());
        });
    }

    @Override
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("[SAGA CHOREOGRAPHY] Nhận sự kiện PAYMENT_COMPLETED cho đơn hàng [{}], PaymentId: {}",
                event.getOrderId(), event.getPaymentId());
        orderRepositoryPort.findByOrderId(event.getOrderId()).ifPresent(order -> {
            order.markPaymentCompleted(event.getPaymentId()); // Domain logic
            Order updatedOrder = orderRepositoryPort.save(order);
            log.info("[ORDER SERVICE] Đơn hàng [{}] đã được [CONFIRMED] thành công 100%!", order.getOrderId());

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

            eventPublisherPort.publishOrderConfirmedEvent(updatedOrder.getOrderId(), confirmedEvent);
        });
    }

    @Override
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.error("[SAGA CHOREOGRAPHY] Nhận sự kiện PAYMENT_FAILED cho đơn hàng [{}], Lý do: {}",
                event.getOrderId(), event.getFailureReason());
        orderRepositoryPort.findByOrderId(event.getOrderId()).ifPresent(order -> {
            order.markPaymentFailed(event.getFailureReason()); // Domain logic
            Order updatedOrder = orderRepositoryPort.save(order);
            log.info("[ORDER SERVICE] Đã cập nhật đơn hàng [{}] thành [PAYMENT_FAILED]", order.getOrderId());

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

            eventPublisherPort.publishOrderCancelledEvent(updatedOrder.getOrderId(), cancelledEvent);
        });
    }
}
