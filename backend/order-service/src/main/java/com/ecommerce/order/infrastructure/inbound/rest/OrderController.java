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
     * Lấy danh sách lịch sử đơn hàng theo query param trên root /api/v1/orders?email=...
     */
    @GetMapping(params = "email")
    public ResponseEntity<List<OrderResponse>> getOrdersByEmailParamOnRoot(@RequestParam("email") String email) {
        List<OrderResponse> orders = orderUseCase.getOrdersByUserEmail(email);
        return ResponseEntity.ok(orders);
    }

    /**
     * Lấy danh sách lịch sử đơn hàng theo query param trên root /api/v1/orders?userId=...
     */
    @GetMapping(params = "userId")
    public ResponseEntity<List<OrderResponse>> getOrdersByUserIdParamOnRoot(@RequestParam("userId") String userId) {
        List<OrderResponse> orders = orderUseCase.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    /**
     * Lấy danh sách lịch sử đơn hàng của người dùng theo User ID.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByUserId(@PathVariable String userId) {
        List<OrderResponse> orders = orderUseCase.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    /**
     * Lấy danh sách lịch sử đơn hàng của khách hàng theo Email (Query Param hoặc Path Variable).
     */
    @GetMapping("/email")
    public ResponseEntity<List<OrderResponse>> getOrdersByEmailParam(@RequestParam("email") String email) {
        List<OrderResponse> orders = orderUseCase.getOrdersByUserEmail(email);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/by-email")
    public ResponseEntity<List<OrderResponse>> getOrdersByEmailQuery(@RequestParam("email") String email) {
        List<OrderResponse> orders = orderUseCase.getOrdersByUserEmail(email);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/email/{userEmail:.+}")
    public ResponseEntity<List<OrderResponse>> getOrdersByUserEmail(@PathVariable("userEmail") String userEmail) {
        List<OrderResponse> orders = orderUseCase.getOrdersByUserEmail(userEmail);
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

    /**
     * Truy vấn chi tiết đơn hàng theo orderId (Khớp mẫu ORD-...).
     */
    @GetMapping("/{orderId:ORD-[A-Za-z0-9-]+}")
    public ResponseEntity<OrderResponse> getOrderByOrderIdRegex(@PathVariable("orderId") String orderId) {
        try {
            OrderResponse response = orderUseCase.getOrderByOrderId(orderId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/detail/{orderId}")
    public ResponseEntity<OrderResponse> getOrderByOrderIdDetail(@PathVariable("orderId") String orderId) {
        try {
            OrderResponse response = orderUseCase.getOrderByOrderId(orderId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
