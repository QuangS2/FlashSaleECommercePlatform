package com.ecommerce.payment.dto;

import com.ecommerce.common.event.payment.PaymentStatus;
import com.ecommerce.payment.domain.entity.PaymentTransaction;
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
public class PaymentResponse {

    private String paymentId;
    private String orderId;
    private String userId;
    private BigDecimal amount;
    private String paymentMethod;
    private PaymentStatus status;
    private String transactionRef;
    private String failureReason;
    private Instant paidAt;
    private Instant createdAt;
    private String message;

    public static PaymentResponse fromEntity(PaymentTransaction txn, String message) {
        return PaymentResponse.builder()
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
                .message(message)
                .build();
    }
}
