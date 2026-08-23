package com.ecommerce.common.event.order;

public enum OrderStatus {
    PENDING,
    INVENTORY_RESERVED,
    CONFIRMED,
    CANCELLED_OUT_OF_STOCK,
    PAYMENT_FAILED,
    CANCELLED
}
