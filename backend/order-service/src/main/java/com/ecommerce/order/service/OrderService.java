package com.ecommerce.order.service;

import com.ecommerce.common.event.inventory.InventoryReservationFailedEvent;
import com.ecommerce.common.event.inventory.InventoryReservedEvent;
import com.ecommerce.common.event.payment.PaymentCompletedEvent;
import com.ecommerce.common.event.payment.PaymentFailedEvent;
import com.ecommerce.order.dto.CreateOrderRequest;
import com.ecommerce.order.dto.OrderResponse;

import java.util.List;

public interface OrderService {

    /**
     * Tiếp nhận đặt hàng Flash Sale (Non-blocking): Tạo Order(PENDING) và phát OrderCreatedEvent.
     */
    OrderResponse createOrder(CreateOrderRequest request);

    /**
     * Lấy chi tiết đơn hàng theo orderId.
     */
    OrderResponse getOrderByOrderId(String orderId);

    /**
     * Lấy danh sách lịch sử đơn hàng của người dùng.
     */
    List<OrderResponse> getOrdersByUserId(String userId);

    /**
     * Xử lý khi Inventory Service đã trừ/giữ kho thành công.
     */
    void handleInventoryReserved(InventoryReservedEvent event);

    /**
     * Xử lý khi Inventory Service báo hết hàng kho (Compensating: Huỷ đơn do hết hàng).
     */
    void handleInventoryReservationFailed(InventoryReservationFailedEvent event);

    /**
     * Xử lý khi Payment Service thanh toán thành công (Happy Path: Đơn hàng CONFIRMED).
     */
    void handlePaymentCompleted(PaymentCompletedEvent event);

    /**
     * Xử lý khi Payment Service thanh toán thất bại (Compensating: Cập nhật PAYMENT_FAILED).
     */
    void handlePaymentFailed(PaymentFailedEvent event);
}
