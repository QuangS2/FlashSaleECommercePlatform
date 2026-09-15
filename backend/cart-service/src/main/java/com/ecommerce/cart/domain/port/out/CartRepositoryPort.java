package com.ecommerce.cart.domain.port.out;

import com.ecommerce.cart.domain.entity.Cart;

import java.util.Optional;

public interface CartRepositoryPort {
    Optional<Cart> findByCustomerId(String customerId);
    void save(Cart cart);
    void deleteByCustomerId(String customerId);
}
