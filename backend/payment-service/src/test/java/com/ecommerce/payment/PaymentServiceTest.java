package com.ecommerce.payment;

import com.ecommerce.common.config.KafkaTopicConstants;
import com.ecommerce.common.event.BaseEvent;
import com.ecommerce.common.event.inventory.InventoryReservedEvent;
import com.ecommerce.common.event.payment.PaymentStatus;
import com.ecommerce.common.kafka.EventPublisherService;
import com.ecommerce.payment.dto.PaymentResponse;
import com.ecommerce.payment.dto.ProcessPaymentRequest;
import com.ecommerce.payment.model.PaymentTransaction;
import com.ecommerce.payment.repository.PaymentRepository;
import com.ecommerce.payment.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private EventPublisherService eventPublisherService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    @DisplayName("Test 1: processPayment - Success saves transaction and publishes PaymentCompletedEvent")
    public void testProcessPayment_Success() {
        String orderId = "ORD-TEST-200";
        ProcessPaymentRequest request = ProcessPaymentRequest.builder()
                .orderId(orderId)
                .userId("user_1001")
                .amount(new BigDecimal("29990000"))
                .paymentMethod("VNPAY")
                .build();

        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(PaymentTransaction.class))).thenAnswer(i -> {
            PaymentTransaction txn = i.getArgument(0);
            txn.setId(1L);
            return txn;
        });

        PaymentResponse response = paymentService.processPayment(request);

        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo(orderId);
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(response.getTransactionRef()).startsWith("TXN-");

        verify(paymentRepository, atLeastOnce()).save(any(PaymentTransaction.class));
        verify(eventPublisherService).publish(eq(KafkaTopicConstants.TOPIC_PAYMENT_EVENTS), eq(orderId), any(BaseEvent.class));
    }

    @Test
    @DisplayName("Test 2: processPayment - Invalid amount fails and publishes PaymentFailedEvent")
    public void testProcessPayment_InvalidAmount() {
        String orderId = "ORD-TEST-201";
        ProcessPaymentRequest request = ProcessPaymentRequest.builder()
                .orderId(orderId)
                .userId("user_1001")
                .amount(new BigDecimal("-50000"))
                .paymentMethod("VNPAY")
                .build();

        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(PaymentTransaction.class))).thenAnswer(i -> i.getArgument(0));

        PaymentResponse response = paymentService.processPayment(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(response.getFailureReason()).contains("không hợp lệ");

        verify(eventPublisherService).publish(eq(KafkaTopicConstants.TOPIC_PAYMENT_EVENTS), eq(orderId), any(BaseEvent.class));
    }

    @Test
    @DisplayName("Test 3: processPayment - Idempotency prevents double charge for already paid order")
    public void testProcessPayment_Idempotent() {
        String orderId = "ORD-ALREADY-PAID";
        PaymentTransaction existing = PaymentTransaction.builder()
                .paymentId("PAY-EXISTING-999")
                .orderId(orderId)
                .userId("user_1001")
                .amount(new BigDecimal("29990000"))
                .paymentMethod("VNPAY")
                .status(PaymentStatus.SUCCESS)
                .transactionRef("TXN-ALREADY-DONE")
                .paidAt(Instant.now())
                .build();

        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(existing));

        ProcessPaymentRequest request = ProcessPaymentRequest.builder()
                .orderId(orderId)
                .amount(new BigDecimal("29990000"))
                .build();

        PaymentResponse response = paymentService.processPayment(request);

        assertThat(response.getPaymentId()).isEqualTo("PAY-EXISTING-999");
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.SUCCESS);

        // Đảm bảo không phát lại sự kiện và không ghi đè bản ghi mới
        verify(eventPublisherService, never()).publish(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("Test 4: handleInventoryReserved - Automatically triggers payment process")
    public void testHandleInventoryReserved() {
        String orderId = "ORD-TEST-202";
        InventoryReservedEvent event = InventoryReservedEvent.builder()
                .orderId(orderId)
                .productId("PROD-1")
                .quantityReserved(1)
                .remainingStock(99)
                .build();

        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(PaymentTransaction.class))).thenAnswer(i -> i.getArgument(0));

        paymentService.handleInventoryReserved(event);

        verify(paymentRepository, atLeastOnce()).save(any(PaymentTransaction.class));
        verify(eventPublisherService).publish(eq(KafkaTopicConstants.TOPIC_PAYMENT_EVENTS), eq(orderId), any(BaseEvent.class));
    }

    @Test
    @DisplayName("Test 5: getPaymentByOrderId - Returns payment transaction")
    public void testGetPaymentByOrderId() {
        String orderId = "ORD-FOUND";
        PaymentTransaction txn = PaymentTransaction.builder()
                .paymentId("PAY-1")
                .orderId(orderId)
                .status(PaymentStatus.SUCCESS)
                .build();

        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(txn));

        PaymentResponse response = paymentService.getPaymentByOrderId(orderId);
        assertThat(response.getPaymentId()).isEqualTo("PAY-1");

        when(paymentRepository.findByOrderId("ORD-NOT-FOUND")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> paymentService.getPaymentByOrderId("ORD-NOT-FOUND"));
    }
}
