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
import com.ecommerce.order.domain.exception.StockReservationException;
import com.ecommerce.order.dto.FlashSaleOrderRequest;
import com.ecommerce.order.infrastructure.persistence.entity.OutboxEventEntity;
import com.ecommerce.order.infrastructure.persistence.repository.SpringDataOutboxEventRepository;
import com.ecommerce.order.infrastructure.redis.RedisLuaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderApplicationService implements OrderUseCase {

    private static final Logger log = LoggerFactory.getLogger(OrderApplicationService.class);

    private final OrderRepositoryPort orderRepositoryPort;
    private final EventPublisherPort eventPublisherPort;
    private final SpringDataOutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final RedisLuaService redisLuaService;

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

        // Ghi nhận bản ghi OutboxEvent đồng thời (Transactional Outbox Pattern - Bảng 14)
        if (outboxEventRepository != null && objectMapper != null) {
            try {
                String payloadJson = objectMapper.writeValueAsString(event);
                OutboxEventEntity outboxEvent = OutboxEventEntity.builder()
                        .id(UUID.randomUUID().toString())
                        .aggregateType("ORDER")
                        .aggregateId(savedOrder.getOrderId())
                        .eventType("OrderCreatedEvent")
                        .payload(payloadJson)
                        .status("PENDING")
                        .retryCount(0)
                        .build();
                outboxEventRepository.save(outboxEvent);
                log.info("[OUTBOX RECORDED] Đã lưu sự kiện Outbox [{}] cho đơn [{}]", outboxEvent.getId(), savedOrder.getOrderId());
            } catch (Exception ex) {
                log.error("[OUTBOX ERROR] Lỗi khi ghi nhận sự kiện Outbox: {}", ex.getMessage());
            }
        }

        eventPublisherPort.publishOrderCreatedEvent(savedOrder.getOrderId(), event);

        return OrderResponse.fromEntity(savedOrder, "Đơn hàng đã được tiếp nhận và đang được điều phối qua Transactional Outbox & Saga Choreography.");
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

    @Override
    public OrderResponse createFlashSaleOrder(FlashSaleOrderRequest request, String customerId) {
        // 1. Giữ chỗ đồng bộ O(1) trên Redis Lua Script
        boolean reserved = redisLuaService.reserveStock(
                request.getSaleId(), request.getItemId(), customerId, request.getQuantity(), 300);
        if (!reserved) {
            throw new StockReservationException("Hết hàng hoặc vượt quá giới hạn mua");
        }

        try {
            // 2. Giao dịch cục bộ MySQL: Lưu Order và OutboxEvent đồng thời
            BigDecimal unitPrice = request.getUnitPrice() != null ? request.getUnitPrice() : BigDecimal.ZERO;
            BigDecimal totalAmount = unitPrice.multiply(BigDecimal.valueOf(request.getQuantity()));
            String email = request.getUserEmail() != null ? request.getUserEmail() : customerId + "@ecommerce.local";

            Order order = Order.builder()
                    .orderId(UUID.randomUUID().toString())
                    .userId(customerId)
                    .userEmail(email)
                    .productId(String.valueOf(request.getItemId()))
                    .productTitle("Flash Sale Item #" + request.getItemId())
                    .quantity(request.getQuantity())
                    .unitPrice(unitPrice)
                    .totalAmount(totalAmount)
                    .status(com.ecommerce.common.event.order.OrderStatus.PENDING)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            Order savedOrder = orderRepositoryPort.save(order);

            try {
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

                OutboxEventEntity outboxEvent = OutboxEventEntity.builder()
                        .id(UUID.randomUUID().toString())
                        .aggregateType("ORDER")
                        .aggregateId(savedOrder.getOrderId())
                        .eventType("OrderCreatedEvent")
                        .payload(objectMapper.writeValueAsString(event))
                        .status("PENDING")
                        .retryCount(0)
                        .createdAt(Instant.now())
                        .build();
                outboxEventRepository.save(outboxEvent);
            } catch (Exception ex) {
                log.error("[OUTBOX ERROR] Flash Sale outbox serialization error: {}", ex.getMessage());
            }

            return OrderResponse.fromEntity(savedOrder, "Đơn hàng Flash Sale đã được tiếp nhận và xử lý");
        } catch (Exception ex) {
            // Bù trừ cục bộ nếu MySQL lỗi: Hoàn kho Redis ngay lập tức (Chống giữ chỗ ảo)
            redisLuaService.releaseReservation(
                    request.getSaleId(), request.getItemId(), customerId, request.getQuantity());
            throw ex;
        }
    }
}
