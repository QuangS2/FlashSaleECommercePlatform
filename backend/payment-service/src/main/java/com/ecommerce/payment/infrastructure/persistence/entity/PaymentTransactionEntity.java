package com.ecommerce.payment.infrastructure.persistence.entity;

import com.ecommerce.common.event.payment.PaymentStatus;
import com.ecommerce.payment.domain.entity.PaymentTransaction;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payment_transactions", indexes = {
        @Index(name = "idx_payments_payment_id", columnList = "payment_id", unique = true),
        @Index(name = "idx_payments_order_id", columnList = "order_id", unique = true),
        @Index(name = "idx_payments_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_id", nullable = false, unique = true, length = 64)
    private String paymentId;

    @Column(name = "order_id", nullable = false, unique = true, length = 64)
    private String orderId;

    @Column(name = "user_id", length = 64)
    private String userId;

    @Column(name = "amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Builder.Default
    @Column(name = "payment_method", length = 32)
    private String paymentMethod = "VNPAY";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private PaymentStatus status;

    @Column(name = "transaction_ref", length = 128)
    private String transactionRef;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = Instant.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public static PaymentTransactionEntity fromDomain(PaymentTransaction txn) {
        return PaymentTransactionEntity.builder()
                .id(txn.getId())
                .paymentId(txn.getPaymentId())
                .orderId(txn.getOrderId())
                .userId(txn.getUserId())
                .amount(txn.getAmount())
                .paymentMethod(txn.getPaymentMethod())
                .status(txn.getStatus())
                .transactionRef(txn.getTransactionRef())
                .failureReason(txn.getFailureReason())
                .paidAt(txn.getPaidAt())
                .createdAt(txn.getCreatedAt())
                .updatedAt(txn.getUpdatedAt())
                .build();
    }

    public PaymentTransaction toDomain() {
        return PaymentTransaction.builder()
                .id(this.id)
                .paymentId(this.paymentId)
                .orderId(this.orderId)
                .userId(this.userId)
                .amount(this.amount)
                .paymentMethod(this.paymentMethod)
                .status(this.status)
                .transactionRef(this.transactionRef)
                .failureReason(this.failureReason)
                .paidAt(this.paidAt)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
}
