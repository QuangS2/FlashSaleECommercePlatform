package com.ecommerce.order.domain.exception;

public class StockReservationException extends RuntimeException {
    public StockReservationException(String message) {
        super(message);
    }
}
