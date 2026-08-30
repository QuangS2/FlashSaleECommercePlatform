package com.ecommerce.payment.infrastructure.inbound.rest;

import com.ecommerce.payment.application.port.in.PaymentUseCase;
import com.ecommerce.payment.dto.PaymentResponse;
import com.ecommerce.payment.dto.ProcessPaymentRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentUseCase paymentUseCase;

    /**
     * Endpoint gọi thanh toán trực tiếp (Dành cho test hoặc thanh toán bù ngoài luồng).
     */
    @PostMapping("/process")
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody ProcessPaymentRequest request) {
        PaymentResponse response = paymentUseCase.processPayment(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Tra cứu thông tin giao dịch theo Order ID.
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(@PathVariable String orderId) {
        PaymentResponse response = paymentUseCase.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(response);
    }

    /**
     * Tra cứu thông tin giao dịch theo Payment ID.
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPaymentByPaymentId(@PathVariable String paymentId) {
        PaymentResponse response = paymentUseCase.getPaymentByPaymentId(paymentId);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint kiểm tra trạng thái sức khỏe service.
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "payment-service",
                "architecture", "Hexagonal Architecture (DDD)",
                "database", "MySQL 8.0"
        ));
    }
}
