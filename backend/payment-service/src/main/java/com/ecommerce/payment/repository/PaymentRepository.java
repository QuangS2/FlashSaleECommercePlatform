package com.ecommerce.payment.repository;

import com.ecommerce.common.event.payment.PaymentStatus;
import com.ecommerce.payment.model.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentTransaction, Long> {

    Optional<PaymentTransaction> findByPaymentId(String paymentId);

    Optional<PaymentTransaction> findByOrderId(String orderId);

    boolean existsByOrderIdAndStatus(String orderId, PaymentStatus status);
}
