package com.ecommerce.payment.domain.port.out;

import com.ecommerce.payment.domain.entity.PaymentTransaction;

import java.util.Optional;

/**
 * Outbound Port for Payment Repository.
 */
public interface PaymentRepositoryPort {

    PaymentTransaction save(PaymentTransaction transaction);

    Optional<PaymentTransaction> findByOrderId(String orderId);

    Optional<PaymentTransaction> findByPaymentId(String paymentId);
}
