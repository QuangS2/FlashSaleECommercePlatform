package com.ecommerce.order.application.port.in;

import com.ecommerce.common.event.inventory.InventoryReservationFailedEvent;
import com.ecommerce.common.event.inventory.InventoryReservedEvent;
import com.ecommerce.common.event.payment.PaymentCompletedEvent;
import com.ecommerce.common.event.payment.PaymentFailedEvent;
import com.ecommerce.order.dto.CreateOrderRequest;
import com.ecommerce.order.dto.OrderResponse;

import java.util.List;

/**
 * Inbound Port for Order Use Cases.
 * Represents the operations that the application exposes to the outside world (Controllers, Listeners).
 */
public interface OrderUseCase {

    OrderResponse createOrder(CreateOrderRequest request);

    OrderResponse getOrderByOrderId(String orderId);

    List<OrderResponse> getOrdersByUserId(String userId);

    List<OrderResponse> getOrdersByUserEmail(String userEmail);

    void handleInventoryReserved(InventoryReservedEvent event);

    void handleInventoryReservationFailed(InventoryReservationFailedEvent event);

    void handlePaymentCompleted(PaymentCompletedEvent event);

    void handlePaymentFailed(PaymentFailedEvent event);
}
