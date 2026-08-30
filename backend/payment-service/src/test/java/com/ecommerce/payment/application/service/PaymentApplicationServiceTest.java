package com.ecommerce.payment.application.service;

import com.ecommerce.common.event.BaseEvent;
import com.ecommerce.common.event.inventory.InventoryReservedEvent;
import com.ecommerce.payment.domain.entity.PaymentTransaction;
import com.ecommerce.payment.domain.port.out.EventPublisherPort;
import com.ecommerce.payment.domain.port.out.PaymentRepositoryPort;
import com.ecommerce.payment.dto.ProcessPaymentRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentApplicationServiceTest {

    @Mock
    private PaymentRepositoryPort paymentRepositoryPort;

    @Mock
    private EventPublisherPort eventPublisherPort;

    @InjectMocks
    private PaymentApplicationService paymentApplicationService;

    @Test
    void testProcessPaymentSuccess() {
        ProcessPaymentRequest request = ProcessPaymentRequest.builder()
                .orderId("order_1")
                .userId("user_1")
                .amount(new BigDecimal("1000.00"))
                .paymentMethod("VNPAY")
                .build();

        when(paymentRepositoryPort.findByOrderId("order_1")).thenReturn(Optional.empty());
        when(paymentRepositoryPort.save(any(PaymentTransaction.class))).thenAnswer(i -> i.getArgument(0));

        paymentApplicationService.processPayment(request);

        verify(paymentRepositoryPort, times(2)).save(any(PaymentTransaction.class)); // 1 for pending, 1 for success
        verify(eventPublisherPort, times(1)).publishPaymentCompletedEvent(eq("order_1"), any(BaseEvent.class));
    }

    @Test
    void testProcessPaymentIdempotent() {
        ProcessPaymentRequest request = ProcessPaymentRequest.builder()
                .orderId("order_1")
                .userId("user_1")
                .amount(new BigDecimal("1000.00"))
                .paymentMethod("VNPAY")
                .build();

        PaymentTransaction existingTxn = PaymentTransaction.create("order_1", "user_1", new BigDecimal("1000.00"), "VNPAY");
        existingTxn.markAsSuccess("TXN-123");

        when(paymentRepositoryPort.findByOrderId("order_1")).thenReturn(Optional.of(existingTxn));

        paymentApplicationService.processPayment(request);

        verify(paymentRepositoryPort, never()).save(any());
        verify(eventPublisherPort, never()).publishPaymentCompletedEvent(anyString(), any());
    }
}
