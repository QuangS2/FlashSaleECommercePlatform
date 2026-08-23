package com.ecommerce.payment.service;

import com.ecommerce.common.event.inventory.InventoryReservedEvent;
import com.ecommerce.payment.dto.PaymentResponse;
import com.ecommerce.payment.dto.ProcessPaymentRequest;

public interface PaymentService {

    /**
     * Xử lý thanh toán Idempotent (chống trừ tiền 2 lần).
     */
    PaymentResponse processPayment(ProcessPaymentRequest request);

    /**
     * Tự động kích hoạt thanh toán khi nhận được sự kiện INVENTORY_RESERVED từ Saga.
     */
    void handleInventoryReserved(InventoryReservedEvent event);

    /**
     * Tra cứu giao dịch thanh toán theo mã đơn hàng.
     */
    PaymentResponse getPaymentByOrderId(String orderId);

    /**
     * Tra cứu giao dịch thanh toán theo mã paymentId.
     */
    PaymentResponse getPaymentByPaymentId(String paymentId);
}
