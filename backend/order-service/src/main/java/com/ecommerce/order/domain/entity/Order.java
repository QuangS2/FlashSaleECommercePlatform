package com.ecommerce.order.domain.entity;

import com.ecommerce.common.event.order.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain Entity for Order.
 * Pure Java object, completely decoupled from JPA/Spring.
 */
public class Order {

    private Long id; // Surrogate key (optional in pure domain, but useful for DB mapping)
    private String orderId; // Business Key
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

    // Private constructor to force using factory methods or Builder
    private Order() {
    }

    // Factory method for creating a NEW order
    public static Order createNew(String userId, String userEmail, String productId, String productTitle, Integer quantity, BigDecimal unitPrice) {
        Order order = new Order();
        order.orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase() + "-" + System.currentTimeMillis();
        order.userId = userId;
        order.userEmail = userEmail;
        order.productId = productId;
        order.productTitle = productTitle != null ? productTitle : "Flash Sale Product";
        
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        order.quantity = quantity;
        
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Unit price must be greater than zero");
        }
        order.unitPrice = unitPrice;
        
        order.totalAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));
        order.status = OrderStatus.PENDING;
        order.createdAt = Instant.now();
        order.updatedAt = Instant.now();
        return order;
    }

    // Builder for reconstructing an existing order from DB
    public static Builder builder() {
        return new Builder();
    }

    // Business Logic Methods (Rich Domain Model)
    public void markInventoryReserved() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("Order must be PENDING to reserve inventory");
        }
        this.status = OrderStatus.INVENTORY_RESERVED;
        this.updatedAt = Instant.now();
    }

    public void markInventoryReservationFailed(String reason) {
        this.status = OrderStatus.CANCELLED_OUT_OF_STOCK;
        this.cancelReason = reason;
        this.updatedAt = Instant.now();
    }

    public void markPaymentCompleted(String paymentId) {
        this.status = OrderStatus.CONFIRMED;
        this.paymentId = paymentId;
        this.updatedAt = Instant.now();
    }

    public void markPaymentFailed(String reason) {
        this.status = OrderStatus.PAYMENT_FAILED;
        this.cancelReason = reason;
        this.updatedAt = Instant.now();
    }

    // Getters only, to protect internal state
    public Long getId() { return id; }
    public String getOrderId() { return orderId; }
    public String getUserId() { return userId; }
    public String getUserEmail() { return userEmail; }
    public String getProductId() { return productId; }
    public String getProductTitle() { return productTitle; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public OrderStatus getStatus() { return status; }
    public String getPaymentId() { return paymentId; }
    public String getCancelReason() { return cancelReason; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public static class Builder {
        private final Order order = new Order();

        public Builder id(Long id) { order.id = id; return this; }
        public Builder orderId(String orderId) { order.orderId = orderId; return this; }
        public Builder userId(String userId) { order.userId = userId; return this; }
        public Builder userEmail(String userEmail) { order.userEmail = userEmail; return this; }
        public Builder productId(String productId) { order.productId = productId; return this; }
        public Builder productTitle(String productTitle) { order.productTitle = productTitle; return this; }
        public Builder quantity(Integer quantity) { order.quantity = quantity; return this; }
        public Builder unitPrice(BigDecimal unitPrice) { order.unitPrice = unitPrice; return this; }
        public Builder totalAmount(BigDecimal totalAmount) { order.totalAmount = totalAmount; return this; }
        public Builder status(OrderStatus status) { order.status = status; return this; }
        public Builder paymentId(String paymentId) { order.paymentId = paymentId; return this; }
        public Builder cancelReason(String cancelReason) { order.cancelReason = cancelReason; return this; }
        public Builder createdAt(Instant createdAt) { order.createdAt = createdAt; return this; }
        public Builder updatedAt(Instant updatedAt) { order.updatedAt = updatedAt; return this; }
        public Order build() { return order; }
    }
}
