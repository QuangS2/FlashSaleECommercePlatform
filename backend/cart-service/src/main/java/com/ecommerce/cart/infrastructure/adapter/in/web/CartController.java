package com.ecommerce.cart.infrastructure.adapter.in.web;

import com.ecommerce.cart.application.dto.AddToCartRequest;
import com.ecommerce.cart.application.dto.CartResponse;
import com.ecommerce.cart.domain.port.in.CartUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartUseCase cartUseCase;

    private String resolveCustomerId(String headerUserId, String queryUserId) {
        if (headerUserId != null) {
            return headerUserId;
        }
        if (queryUserId != null) {
            return queryUserId;
        }
        return "anonymous-guest";
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @RequestParam(value = "userId", required = false) String queryUserId) {
        String customerId = resolveCustomerId(headerUserId, queryUserId);
        return ResponseEntity.ok(cartUseCase.getCart(customerId));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addToCart(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @RequestParam(value = "userId", required = false) String queryUserId,
            @RequestBody AddToCartRequest request) {
        String customerId = resolveCustomerId(headerUserId, queryUserId);
        return ResponseEntity.ok(cartUseCase.addToCart(customerId, request));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponse> removeFromCart(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @RequestParam(value = "userId", required = false) String queryUserId,
            @PathVariable String productId) {
        String customerId = resolveCustomerId(headerUserId, queryUserId);
        return ResponseEntity.ok(cartUseCase.removeFromCart(customerId, productId));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @RequestParam(value = "userId", required = false) String queryUserId) {
        String customerId = resolveCustomerId(headerUserId, queryUserId);
        cartUseCase.clearCart(customerId);
        return ResponseEntity.noContent().build();
    }
}
