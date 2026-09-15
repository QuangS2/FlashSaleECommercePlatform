package com.ecommerce.payment.infrastructure.persistence.adapter;

import com.ecommerce.common.event.payment.PaymentStatus;
import com.ecommerce.payment.domain.entity.PaymentTransaction;
import com.ecommerce.payment.infrastructure.persistence.entity.PaymentTransactionEntity;
import com.ecommerce.payment.infrastructure.persistence.repository.SpringDataPaymentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentJpaAdapterTest {

    @Mock
    private SpringDataPaymentRepository repository;

    @InjectMocks
    private PaymentJpaAdapter adapter;

    @Test
    @DisplayName("Test 1: save transaction successfully converts and returns domain")
    void testSaveSuccess() {
        PaymentTransaction tx = PaymentTransaction.create("ORD-1", "user-1", new BigDecimal("100.00"), "MOCK");
        PaymentTransactionEntity entity = PaymentTransactionEntity.fromDomain(tx);
        entity.setId(1L);

        when(repository.save(any(PaymentTransactionEntity.class))).thenReturn(entity);

        PaymentTransaction result = adapter.save(tx);

        assertNotNull(result);
        assertEquals("ORD-1", result.getOrderId());
        verify(repository, times(1)).save(any());
    }

    @Test
    @DisplayName("Test 2: findByOrderId found returns domain object")
    void testFindByOrderIdFound() {
        PaymentTransaction tx = PaymentTransaction.create("ORD-2", "user-2", new BigDecimal("50.00"), "MOCK");
        PaymentTransactionEntity entity = PaymentTransactionEntity.fromDomain(tx);

        when(repository.findByOrderId("ORD-2")).thenReturn(Optional.of(entity));

        Optional<PaymentTransaction> result = adapter.findByOrderId("ORD-2");

        assertTrue(result.isPresent());
        assertEquals("ORD-2", result.get().getOrderId());
    }

    @Test
    @DisplayName("Test 3: findByOrderId not found returns empty")
    void testFindByOrderIdNotFound() {
        when(repository.findByOrderId("UNKNOWN")).thenReturn(Optional.empty());

        Optional<PaymentTransaction> result = adapter.findByOrderId("UNKNOWN");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Test 4: findByPaymentId found returns domain object")
    void testFindByPaymentIdFound() {
        PaymentTransaction tx = PaymentTransaction.create("ORD-4", "user-4", new BigDecimal("75.00"), "MOCK");
        PaymentTransactionEntity entity = PaymentTransactionEntity.fromDomain(tx);

        when(repository.findByPaymentId("PAY-4")).thenReturn(Optional.of(entity));

        Optional<PaymentTransaction> result = adapter.findByPaymentId("PAY-4");

        assertTrue(result.isPresent());
        assertEquals("ORD-4", result.get().getOrderId());
    }

    @Test
    @DisplayName("Test 5: findByPaymentId not found returns empty")
    void testFindByPaymentIdNotFound() {
        when(repository.findByPaymentId("PAY-NOT-EXIST")).thenReturn(Optional.empty());

        Optional<PaymentTransaction> result = adapter.findByPaymentId("PAY-NOT-EXIST");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Test 6: entity conversion methods null checks")
    void testEntityFromDomainAndToDomain() {
        PaymentTransaction tx = PaymentTransaction.create("ORD-6", "user-6", new BigDecimal("12.00"), "MOCK");
        PaymentTransactionEntity entity = PaymentTransactionEntity.fromDomain(tx);
        assertNotNull(entity);
        PaymentTransaction domain = entity.toDomain();
        assertNotNull(domain);
        assertEquals(tx.getOrderId(), domain.getOrderId());
        assertEquals(tx.getStatus(), domain.getStatus());
    }
}
