package com.ecommerce.order.dto;

import com.ecommerce.common.event.order.OrderStatus;
import com.ecommerce.order.domain.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    private String orderId;
    private String userId;
    private String userEmail;
    private String productId;
    private String productTitle;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private String paymentId;
    private String cancelReason;
    private Instant createdAt;
    private Instant updatedAt;
    private String message;

    public static OrderResponse fromEntity(Order order, String message) {
        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .userId(order.getUserId())
                .userEmail(order.getUserEmail())
                .productId(order.getProductId())
                .productTitle(order.getProductTitle())
                .quantity(order.getQuantity())
                .unitPrice(order.getUnitPrice())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .paymentId(order.getPaymentId())
                .cancelReason(order.getCancelReason())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .message(message)
                .build();
    }
}
