package com.ecommerce.cart.domain.port.in;

import com.ecommerce.cart.application.dto.AddToCartRequest;
import com.ecommerce.cart.application.dto.CartResponse;

public interface CartUseCase {
    CartResponse getCart(String customerId);
    CartResponse addToCart(String customerId, AddToCartRequest request);
    CartResponse removeFromCart(String customerId, String productId);
    void clearCart(String customerId);
}
