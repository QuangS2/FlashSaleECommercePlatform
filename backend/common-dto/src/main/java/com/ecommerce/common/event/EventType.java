package com.ecommerce.common.event;

public enum EventType {
    // Order Domain Events
    ORDER_CREATED,
    ORDER_CONFIRMED,
    ORDER_CANCELLED,

    // Inventory Domain Events
    INVENTORY_RESERVED,
    INVENTORY_RESERVATION_FAILED,
    INVENTORY_RESTORED,
    STOCK_UPDATED,

    // Payment Domain Events
    PAYMENT_COMPLETED,
    PAYMENT_FAILED,

    // Notification Domain Events
    NOTIFICATION_DISPATCH
}
