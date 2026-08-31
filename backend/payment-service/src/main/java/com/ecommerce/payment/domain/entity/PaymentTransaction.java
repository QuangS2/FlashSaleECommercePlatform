package com.ecommerce.payment.domain.entity;

import com.ecommerce.common.event.payment.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Domain Entity for Payment Transaction.
 * Pure Java object encapsulating payment business logic.
 */
public class PaymentTransaction {

    private Long id; // Surrogate key (optional in domain)
    private String paymentId; // Business Key
    private String orderId;
    private String userId;
    private BigDecimal amount;
    private String paymentMethod;
    private PaymentStatus status;
    private String transactionRef;
    private String failureReason;
    private Instant paidAt;
    private Instant createdAt;
    private Instant updatedAt;

    private PaymentTransaction() {
    }

    /**
     * Initializes a new payment transaction.
     */
    public static PaymentTransaction create(String orderId, String userId, BigDecimal amount, String paymentMethod) {
        PaymentTransaction txn = new PaymentTransaction();
        txn.paymentId = "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase() + "-" + System.currentTimeMillis();
        txn.orderId = orderId;
        txn.userId = userId != null ? userId : "unknown-user";
        txn.amount = amount;
        txn.paymentMethod = paymentMethod != null ? paymentMethod : "VNPAY";
        txn.status = PaymentStatus.PENDING;
        txn.createdAt = Instant.now();
        txn.updatedAt = Instant.now();
        return txn;
    }

    public static Builder builder() {
        return new Builder();
    }

    // Business Logic Methods

    /**
     * Idempotency Check: Determines if this payment has already been successfully processed.
     */
    public boolean isAlreadyPaid() {
        return this.status == PaymentStatus.SUCCESS;
    }

    /**
     * Updates the payment amount and resets status to PENDING if not already paid.
     */
    public void process(BigDecimal newAmount) {
        if (isAlreadyPaid()) {
            throw new IllegalStateException("Cannot process an already paid transaction.");
        }
        this.amount = newAmount;
        this.status = PaymentStatus.PENDING;
        this.updatedAt = Instant.now();
    }

    /**
     * Marks the transaction as failed.
     */
    public void markAsFailed(String reason) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
        this.updatedAt = Instant.now();
    }

    /**
     * Marks the transaction as successfully paid.
     */
    public void markAsSuccess(String txnRef) {
        this.status = PaymentStatus.SUCCESS;
        this.transactionRef = txnRef;
        this.paidAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    // Getters
    public Long getId() { return id; }
    public String getPaymentId() { return paymentId; }
    public String getOrderId() { return orderId; }
    public String getUserId() { return userId; }
    public BigDecimal getAmount() { return amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public PaymentStatus getStatus() { return status; }
    public String getTransactionRef() { return transactionRef; }
    public String getFailureReason() { return failureReason; }
    public Instant getPaidAt() { return paidAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public static class Builder {
        private final PaymentTransaction txn = new PaymentTransaction();

        public Builder id(Long id) { txn.id = id; return this; }
        public Builder paymentId(String paymentId) { txn.paymentId = paymentId; return this; }
        public Builder orderId(String orderId) { txn.orderId = orderId; return this; }
        public Builder userId(String userId) { txn.userId = userId; return this; }
        public Builder amount(BigDecimal amount) { txn.amount = amount; return this; }
        public Builder paymentMethod(String paymentMethod) { txn.paymentMethod = paymentMethod; return this; }
        public Builder status(PaymentStatus status) { txn.status = status; return this; }
        public Builder transactionRef(String transactionRef) { txn.transactionRef = transactionRef; return this; }
        public Builder failureReason(String failureReason) { txn.failureReason = failureReason; return this; }
        public Builder paidAt(Instant paidAt) { txn.paidAt = paidAt; return this; }
        public Builder createdAt(Instant createdAt) { txn.createdAt = createdAt; return this; }
        public Builder updatedAt(Instant updatedAt) { txn.updatedAt = updatedAt; return this; }
        
        public PaymentTransaction build() { return txn; }
    }
}
