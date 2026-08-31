package com.ecommerce.order.infrastructure.persistence.entity;

import com.ecommerce.common.event.order.OrderStatus;
import com.ecommerce.order.domain.entity.Order;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_orders_order_id", columnList = "order_id", unique = true),
        @Index(name = "idx_orders_user_id", columnList = "user_id"),
        @Index(name = "idx_orders_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, unique = true, length = 64)
    private String orderId;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(name = "user_email", length = 128)
    private String userEmail;

    @Column(name = "product_id", nullable = false, length = 64)
    private String productId;

    @Column(name = "product_title", length = 255)
    private String productTitle;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 14, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private OrderStatus status;

    @Column(name = "payment_id", length = 64)
    private String paymentId;

    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // Mapper methods
    public static OrderEntity fromDomain(Order order) {
        return OrderEntity.builder()
                .id(order.getId())
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
                .build();
    }

    public Order toDomain() {
        return Order.builder()
                .id(this.id)
                .orderId(this.orderId)
                .userId(this.userId)
                .userEmail(this.userEmail)
                .productId(this.productId)
                .productTitle(this.productTitle)
                .quantity(this.quantity)
                .unitPrice(this.unitPrice)
                .totalAmount(this.totalAmount)
                .status(this.status)
                .paymentId(this.paymentId)
                .cancelReason(this.cancelReason)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
}
