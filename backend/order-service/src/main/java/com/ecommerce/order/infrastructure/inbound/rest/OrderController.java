package com.ecommerce.order.infrastructure.inbound.rest;

import com.ecommerce.order.application.port.in.OrderUseCase;
import com.ecommerce.order.dto.CreateOrderRequest;
import com.ecommerce.order.dto.OrderResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderUseCase orderUseCase;

    /**
     * Tiếp nhận đơn hàng Flash Sale (Non-blocking: HTTP 202 Accepted).
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse response = orderUseCase.createOrder(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /**
     * Truy vấn chi tiết đơn hàng theo orderId.
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderByOrderId(@PathVariable String orderId) {
        OrderResponse response = orderUseCase.getOrderByOrderId(orderId);
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy danh sách lịch sử đơn hàng của người dùng.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByUserId(@PathVariable String userId) {
        List<OrderResponse> orders = orderUseCase.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    /**
     * Endpoint kiểm tra trạng thái sức khỏe service.
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "order-service",
                "architecture", "Hexagonal Architecture (DDD) & Event-Driven Saga Choreography",
                "database", "MySQL 8.0"
        ));
    }
}
