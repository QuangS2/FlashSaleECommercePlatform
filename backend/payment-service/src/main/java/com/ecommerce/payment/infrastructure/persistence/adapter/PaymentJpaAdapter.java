package com.ecommerce.payment.infrastructure.persistence.adapter;

import com.ecommerce.payment.domain.entity.PaymentTransaction;
import com.ecommerce.payment.domain.port.out.PaymentRepositoryPort;
import com.ecommerce.payment.infrastructure.persistence.entity.PaymentTransactionEntity;
import com.ecommerce.payment.infrastructure.persistence.repository.SpringDataPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PaymentJpaAdapter implements PaymentRepositoryPort {

    private final SpringDataPaymentRepository springDataPaymentRepository;

    @Override
    public PaymentTransaction save(PaymentTransaction transaction) {
        PaymentTransactionEntity entity = PaymentTransactionEntity.fromDomain(transaction);
        PaymentTransactionEntity savedEntity = springDataPaymentRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<PaymentTransaction> findByOrderId(String orderId) {
        return springDataPaymentRepository.findByOrderId(orderId)
                .map(PaymentTransactionEntity::toDomain);
    }

    @Override
    public Optional<PaymentTransaction> findByPaymentId(String paymentId) {
        return springDataPaymentRepository.findByPaymentId(paymentId)
                .map(PaymentTransactionEntity::toDomain);
    }
}
