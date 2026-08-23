package com.ecommerce.payment.service.impl;

import com.ecommerce.common.config.KafkaTopicConstants;
import com.ecommerce.common.event.BaseEvent;
import com.ecommerce.common.event.EventType;
import com.ecommerce.common.event.inventory.InventoryReservedEvent;
import com.ecommerce.common.event.payment.PaymentCompletedEvent;
import com.ecommerce.common.event.payment.PaymentFailedEvent;
import com.ecommerce.common.event.payment.PaymentStatus;
import com.ecommerce.common.kafka.EventPublisherService;
import com.ecommerce.payment.dto.PaymentResponse;
import com.ecommerce.payment.dto.ProcessPaymentRequest;
import com.ecommerce.payment.model.PaymentTransaction;
import com.ecommerce.payment.repository.PaymentRepository;
import com.ecommerce.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentRepository paymentRepository;
    private final EventPublisherService eventPublisherService;

    @Override
    @Transactional
    public PaymentResponse processPayment(ProcessPaymentRequest request) {
        String orderId = request.getOrderId();

        // 1. Kiểm tra tính Idempotent (Chống trừ tiền 2 lần)
        Optional<PaymentTransaction> existingOpt = paymentRepository.findByOrderId(orderId);
        if (existingOpt.isPresent() && existingOpt.get().getStatus() == PaymentStatus.SUCCESS) {
            log.info("[PAYMENT IDEMPOTENT] Đơn hàng [{}] đã thanh toán thành công trước đó (PaymentId: {})",
                    orderId, existingOpt.get().getPaymentId());
            return PaymentResponse.fromEntity(existingOpt.get(), "Giao dịch đã được thanh toán thành công trước đó (Idempotent).");
        }

        String paymentId = existingOpt.map(PaymentTransaction::getPaymentId)
                .orElse("PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase() + "-" + System.currentTimeMillis());

        PaymentTransaction txn = existingOpt.orElse(PaymentTransaction.builder()
                .paymentId(paymentId)
                .orderId(orderId)
                .userId(request.getUserId() != null ? request.getUserId() : "unknown-user")
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "VNPAY")
                .status(PaymentStatus.PENDING)
                .build());

        txn.setAmount(request.getAmount());
        txn.setStatus(PaymentStatus.PENDING);
        txn = paymentRepository.save(txn);

        // 2. Mô phỏng xử lý Cổng Thanh toán (Payment Gateway Simulation)
        // Kịch bản thất bại: Số tiền không hợp lệ hoặc giả lập lỗi thẻ (ví dụ amount <= 0)
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            txn.setStatus(PaymentStatus.FAILED);
            txn.setFailureReason("Số tiền thanh toán không hợp lệ (Phải lớn hơn 0)");
            PaymentTransaction savedFailed = paymentRepository.save(txn);

            log.error("[PAYMENT FAILED] Thanh toán thất bại cho đơn hàng [{}] - Lý do: {}", orderId, savedFailed.getFailureReason());

            // Phát sự kiện PAYMENT_FAILED (kích hoạt bù trừ Saga)
            publishPaymentFailed(savedFailed.getPaymentId(), savedFailed.getOrderId(), savedFailed.getUserId(), savedFailed.getAmount(), savedFailed.getFailureReason());
            return PaymentResponse.fromEntity(savedFailed, "Thanh toán thất bại: " + savedFailed.getFailureReason());
        }

        // Kịch bản Thành công (Happy Path)
        txn.setStatus(PaymentStatus.SUCCESS);
        txn.setTransactionRef("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        txn.setPaidAt(Instant.now());
        PaymentTransaction savedSuccess = paymentRepository.save(txn);

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

        eventPublisherService.publish(KafkaTopicConstants.TOPIC_PAYMENT_EVENTS, savedSuccess.getOrderId(), event);

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

        eventPublisherService.publish(KafkaTopicConstants.TOPIC_PAYMENT_EVENTS, orderId, event);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId)
                .map(txn -> PaymentResponse.fromEntity(txn, "Tra cứu thanh toán thành công"))
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giao dịch thanh toán cho đơn hàng: " + orderId));
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByPaymentId(String paymentId) {
        return paymentRepository.findByPaymentId(paymentId)
                .map(txn -> PaymentResponse.fromEntity(txn, "Tra cứu thanh toán thành công"))
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giao dịch với mã: " + paymentId));
    }
}
