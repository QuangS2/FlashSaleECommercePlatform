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
import static org.junit.jupiter.api.Assertions.*;
import com.ecommerce.common.event.payment.PaymentStatus;

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

        com.ecommerce.payment.dto.PaymentResponse response = paymentApplicationService.processPayment(request);

        assertNotNull(response);
        assertEquals("order_1", response.getOrderId());
        assertEquals(PaymentStatus.SUCCESS, response.getStatus());

        verify(paymentRepositoryPort, times(2)).save(any(PaymentTransaction.class)); // 1 for pending, 1 for success
        verify(eventPublisherPort, times(1)).publishPaymentCompletedEvent(eq("order_1"), any(BaseEvent.class));
    }

    @Test
    void testProcessPaymentIdempotent_AlreadyPaid() {
        ProcessPaymentRequest request = ProcessPaymentRequest.builder()
                .orderId("order_1")
                .userId("user_1")
                .amount(new BigDecimal("1000.00"))
                .paymentMethod("VNPAY")
                .build();

        PaymentTransaction existingTxn = PaymentTransaction.create("order_1", "user_1", new BigDecimal("1000.00"), "VNPAY");
        existingTxn.markAsSuccess("TXN-123");

        when(paymentRepositoryPort.findByOrderId("order_1")).thenReturn(Optional.of(existingTxn));

        com.ecommerce.payment.dto.PaymentResponse response = paymentApplicationService.processPayment(request);

        assertNotNull(response);
        assertEquals(PaymentStatus.SUCCESS, response.getStatus());
        assertEquals("Giao dịch đã được thanh toán thành công trước đó (Idempotent).", response.getMessage());

        verify(paymentRepositoryPort, never()).save(any());
        verify(eventPublisherPort, never()).publishPaymentCompletedEvent(anyString(), any());
    }

    @Test
    void testProcessPaymentIdempotent_Pending() {
        ProcessPaymentRequest request = ProcessPaymentRequest.builder()
                .orderId("order_2")
                .userId("user_2")
                .amount(new BigDecimal("1000.00"))
                .paymentMethod("VNPAY")
                .build();

        PaymentTransaction existingTxn = PaymentTransaction.create("order_2", "user_2", new BigDecimal("1000.00"), "VNPAY");
        // Status is PENDING by default

        when(paymentRepositoryPort.findByOrderId("order_2")).thenReturn(Optional.of(existingTxn));
        when(paymentRepositoryPort.save(any(PaymentTransaction.class))).thenAnswer(i -> i.getArgument(0));

        com.ecommerce.payment.dto.PaymentResponse response = paymentApplicationService.processPayment(request);

        assertNotNull(response);
        assertEquals("order_2", response.getOrderId());
        assertEquals(PaymentStatus.SUCCESS, response.getStatus());

        verify(paymentRepositoryPort, times(2)).save(any(PaymentTransaction.class));
        verify(eventPublisherPort, times(1)).publishPaymentCompletedEvent(eq("order_2"), any(BaseEvent.class));
    }

    @Test
    void testProcessPaymentFailed_AmountZero() {
        ProcessPaymentRequest request = ProcessPaymentRequest.builder()
                .orderId("order_zero")
                .userId("user_1")
                .amount(BigDecimal.ZERO)
                .paymentMethod("VNPAY")
                .build();

        when(paymentRepositoryPort.findByOrderId("order_zero")).thenReturn(Optional.empty());
        when(paymentRepositoryPort.save(any(PaymentTransaction.class))).thenAnswer(i -> i.getArgument(0));

        com.ecommerce.payment.dto.PaymentResponse response = paymentApplicationService.processPayment(request);

        assertNotNull(response);
        assertEquals(PaymentStatus.FAILED, response.getStatus());
        assertTrue(response.getMessage().contains("Thanh toán thất bại"));

        verify(paymentRepositoryPort, times(2)).save(any(PaymentTransaction.class));
        verify(eventPublisherPort, never()).publishPaymentCompletedEvent(anyString(), any());
        verify(eventPublisherPort, times(1)).publishPaymentFailedEvent(eq("order_zero"), any(BaseEvent.class));
    }

    @Test
    void testProcessPaymentFailed_AmountNegative() {
        ProcessPaymentRequest request = ProcessPaymentRequest.builder()
                .orderId("order_neg")
                .userId("user_1")
                .amount(new BigDecimal("-100.00"))
                .paymentMethod("VNPAY")
                .build();

        when(paymentRepositoryPort.findByOrderId("order_neg")).thenReturn(Optional.empty());
        when(paymentRepositoryPort.save(any(PaymentTransaction.class))).thenAnswer(i -> i.getArgument(0));

        com.ecommerce.payment.dto.PaymentResponse response = paymentApplicationService.processPayment(request);

        assertNotNull(response);
        assertEquals(PaymentStatus.FAILED, response.getStatus());

        verify(eventPublisherPort, times(1)).publishPaymentFailedEvent(eq("order_neg"), any(BaseEvent.class));
    }

    @Test
    void testHandleInventoryReserved() {
        InventoryReservedEvent event = InventoryReservedEvent.builder()
                .orderId("order_inv")
                .build();

        when(paymentRepositoryPort.findByOrderId("order_inv")).thenReturn(Optional.empty());
        when(paymentRepositoryPort.save(any(PaymentTransaction.class))).thenAnswer(i -> i.getArgument(0));

        paymentApplicationService.handleInventoryReserved(event);

        verify(paymentRepositoryPort, times(2)).save(any(PaymentTransaction.class));
        verify(eventPublisherPort, times(1)).publishPaymentCompletedEvent(eq("order_inv"), any(BaseEvent.class));
    }

    @Test
    void testGetPaymentByOrderId_Success() {
        PaymentTransaction txn = PaymentTransaction.create("order_1", "user_1", new BigDecimal("100.00"), "VNPAY");
        when(paymentRepositoryPort.findByOrderId("order_1")).thenReturn(Optional.of(txn));

        com.ecommerce.payment.dto.PaymentResponse response = paymentApplicationService.getPaymentByOrderId("order_1");
        assertNotNull(response);
        assertEquals("order_1", response.getOrderId());
    }

    @Test
    void testGetPaymentByOrderId_NotFound() {
        when(paymentRepositoryPort.findByOrderId("order_not_found")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> 
            paymentApplicationService.getPaymentByOrderId("order_not_found")
        );
    }

    @Test
    void testGetPaymentByPaymentId_Success() {
        PaymentTransaction txn = PaymentTransaction.create("order_1", "user_1", new BigDecimal("100.00"), "VNPAY");
        when(paymentRepositoryPort.findByPaymentId(txn.getPaymentId())).thenReturn(Optional.of(txn));

        com.ecommerce.payment.dto.PaymentResponse response = paymentApplicationService.getPaymentByPaymentId(txn.getPaymentId());
        assertNotNull(response);
        assertEquals(txn.getPaymentId(), response.getPaymentId());
    }

    @Test
    void testGetPaymentByPaymentId_NotFound() {
        when(paymentRepositoryPort.findByPaymentId("invalid_id")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> 
            paymentApplicationService.getPaymentByPaymentId("invalid_id")
        );
    }
}
