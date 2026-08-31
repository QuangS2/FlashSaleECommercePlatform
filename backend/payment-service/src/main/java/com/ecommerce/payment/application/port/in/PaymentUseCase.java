package com.ecommerce.payment.application.port.in;

import com.ecommerce.common.event.inventory.InventoryReservedEvent;
import com.ecommerce.payment.dto.PaymentResponse;
import com.ecommerce.payment.dto.ProcessPaymentRequest;

/**
 * Inbound Port for Payment Use Cases.
 */
public interface PaymentUseCase {

    PaymentResponse processPayment(ProcessPaymentRequest request);

    void handleInventoryReserved(InventoryReservedEvent event);

    PaymentResponse getPaymentByOrderId(String orderId);

    PaymentResponse getPaymentByPaymentId(String paymentId);
}
