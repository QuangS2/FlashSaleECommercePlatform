package com.ecommerce.payment.application.service;

import com.ecommerce.common.event.BaseEvent;
import com.ecommerce.common.event.EventType;
import com.ecommerce.common.event.inventory.InventoryReservedEvent;
import com.ecommerce.common.event.payment.PaymentCompletedEvent;
import com.ecommerce.common.event.payment.PaymentFailedEvent;
import com.ecommerce.common.event.payment.PaymentStatus;
import com.ecommerce.payment.application.port.in.PaymentUseCase;
import com.ecommerce.payment.domain.entity.PaymentTransaction;
import com.ecommerce.payment.domain.port.out.EventPublisherPort;
import com.ecommerce.payment.domain.port.out.PaymentRepositoryPort;
import com.ecommerce.payment.dto.PaymentResponse;
import com.ecommerce.payment.dto.ProcessPaymentRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Application Service for Payment.
 * Orchestrates domain logic, repositories, and events.
 */
@Service
@RequiredArgsConstructor
public class PaymentApplicationService implements PaymentUseCase {

    private static final Logger log = LoggerFactory.getLogger(PaymentApplicationService.class);

    private final PaymentRepositoryPort paymentRepositoryPort;
    private final EventPublisherPort eventPublisherPort;

    @Override
    @Transactional
    public PaymentResponse processPayment(ProcessPaymentRequest request) {
        String orderId = request.getOrderId();

        // 1. Kiểm tra tính Idempotent (Chống trừ tiền 2 lần) thông qua Domain Logic
        Optional<PaymentTransaction> existingOpt = paymentRepositoryPort.findByOrderId(orderId);
        if (existingOpt.isPresent()) {
            PaymentTransaction existingTxn = existingOpt.get();
            if (existingTxn.isAlreadyPaid()) {
                log.info("[PAYMENT IDEMPOTENT] Đơn hàng [{}] đã thanh toán thành công trước đó (PaymentId: {})",
                        orderId, existingTxn.getPaymentId());
                return PaymentResponse.fromEntity(existingTxn, "Giao dịch đã được thanh toán thành công trước đó (Idempotent).");
            }
        }

        PaymentTransaction txn = existingOpt.orElse(PaymentTransaction.create(
                orderId,
                request.getUserId(),
                request.getAmount(),
                request.getPaymentMethod()
        ));

        txn.process(request.getAmount());
        txn = paymentRepositoryPort.save(txn);

        // 2. Mô phỏng xử lý Cổng Thanh toán (Payment Gateway Simulation)
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            txn.markAsFailed("Số tiền thanh toán không hợp lệ (Phải lớn hơn 0)");
            PaymentTransaction savedFailed = paymentRepositoryPort.save(txn);

            log.error("[PAYMENT FAILED] Thanh toán thất bại cho đơn hàng [{}] - Lý do: {}", orderId, savedFailed.getFailureReason());

            publishPaymentFailed(savedFailed.getPaymentId(), savedFailed.getOrderId(), savedFailed.getUserId(), savedFailed.getAmount(), savedFailed.getFailureReason());
            return PaymentResponse.fromEntity(savedFailed, "Thanh toán thất bại: " + savedFailed.getFailureReason());
        }

        // Kịch bản Thành công (Happy Path)
        txn.markAsSuccess("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        PaymentTransaction savedSuccess = paymentRepositoryPort.save(txn);

        log.info("[PAYMENT SUCCESS] Thanh toán thành công 100% cho đơn [{}] - PaymentId: {}, TxnRef: {}, Số tiền: {}",
                savedSuccess.getOrderId(), savedSuccess.getPaymentId(), savedSuccess.getTransactionRef(), savedSuccess.getAmount());

        // Phát sự kiện PAYMENT_COMPLETED lên Kafka
        PaymentCompletedEvent payload = PaymentCompletedEvent.builder()
                .paymentId(savedSuccess.getPaymentId())
                .orderId(savedSuccess.getOrderId())
                .userId(savedSuccess.getUserId())
                .amount(savedSuccess.getAmount())
                .paymentMethod(savedSuccess.getPaymentMethod())
                .transactionReference(savedSuccess.getTransactionRef())
                .status(PaymentStatus.SUCCESS)
                .completedAt(savedSuccess.getPaidAt())
                .build();

        BaseEvent<PaymentCompletedEvent> event = BaseEvent.of(
                EventType.PAYMENT_COMPLETED,
                "CORR-" + savedSuccess.getOrderId(),
                "payment-service",
                payload
        );

        eventPublisherPort.publishPaymentCompletedEvent(savedSuccess.getOrderId(), event);

        return PaymentResponse.fromEntity(savedSuccess, "Thanh toán thành công qua cổng " + savedSuccess.getPaymentMethod());
    }

    @Override
    @Transactional
    public void handleInventoryReserved(InventoryReservedEvent event) {
        log.info("[SAGA CHOREOGRAPHY] Nhận sự kiện INVENTORY_RESERVED cho đơn hàng [{}]. Bắt đầu tiến trình thanh toán...",
                event.getOrderId());

        ProcessPaymentRequest request = ProcessPaymentRequest.builder()
                .orderId(event.getOrderId())
                .amount(new BigDecimal("29990000")) // Mặc định đơn giá Flash Sale nếu event không kèm giá
                .paymentMethod("VNPAY")
                .build();

        processPayment(request);
    }

    private void publishPaymentFailed(String paymentId, String orderId, String userId, BigDecimal amount, String reason) {
        PaymentFailedEvent payload = PaymentFailedEvent.builder()
                .paymentId(paymentId)
                .orderId(orderId)
                .userId(userId)
                .amount(amount)
                .failureReason(reason)
                .status(PaymentStatus.FAILED)
                .failedAt(Instant.now())
                .build();

        BaseEvent<PaymentFailedEvent> event = BaseEvent.of(
                EventType.PAYMENT_FAILED,
                "CORR-" + orderId,
                "payment-service",
                payload
        );

        eventPublisherPort.publishPaymentFailedEvent(orderId, event);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(String orderId) {
        return paymentRepositoryPort.findByOrderId(orderId)
                .map(txn -> PaymentResponse.fromEntity(txn, "Tra cứu thanh toán thành công"))
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giao dịch thanh toán cho đơn hàng: " + orderId));
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByPaymentId(String paymentId) {
        return paymentRepositoryPort.findByPaymentId(paymentId)
                .map(txn -> PaymentResponse.fromEntity(txn, "Tra cứu thanh toán thành công"))
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giao dịch với mã: " + paymentId));
    }
}
