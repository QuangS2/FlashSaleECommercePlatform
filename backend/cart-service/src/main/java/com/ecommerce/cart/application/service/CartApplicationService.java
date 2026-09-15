package com.ecommerce.cart.application.service;

import com.ecommerce.cart.application.dto.AddToCartRequest;
import com.ecommerce.cart.application.dto.CartResponse;
import com.ecommerce.cart.domain.entity.Cart;
import com.ecommerce.cart.domain.entity.CartItem;
import com.ecommerce.cart.domain.port.in.CartUseCase;
import com.ecommerce.cart.domain.port.out.CartRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CartApplicationService implements CartUseCase {

    private final CartRepositoryPort cartRepositoryPort;

    @Override
    public CartResponse getCart(String customerId) {
        Cart cart = cartRepositoryPort.findByCustomerId(customerId)
                .orElseGet(() -> Cart.builder()
                        .customerId(customerId)
                        .items(new ArrayList<>())
                        .build());
        return CartResponse.fromDomain(cart);
    }

    @Override
    public CartResponse addToCart(String customerId, AddToCartRequest request) {
        Cart cart = cartRepositoryPort.findByCustomerId(customerId)
                .orElseGet(() -> Cart.builder()
                        .customerId(customerId)
                        .items(new ArrayList<>())
                        .build());

        CartItem item = CartItem.builder()
                .productId(request.getProductId())
                .productTitle(request.getProductTitle())
                .imageUrl(request.getImageUrl())
                .quantity(request.getQuantity())
                .unitPrice(request.getUnitPrice())
                .build();

        cart.addItem(item);
        cartRepositoryPort.save(cart);

        return CartResponse.fromDomain(cart);
    }

    @Override
    public CartResponse removeFromCart(String customerId, String productId) {
        Cart cart = cartRepositoryPort.findByCustomerId(customerId)
                .orElseGet(() -> Cart.builder()
                        .customerId(customerId)
                        .items(new ArrayList<>())
                        .build());

        cart.removeItem(productId);
        cartRepositoryPort.save(cart);

        return CartResponse.fromDomain(cart);
    }

    @Override
    public void clearCart(String customerId) {
        cartRepositoryPort.deleteByCustomerId(customerId);
    }
}
