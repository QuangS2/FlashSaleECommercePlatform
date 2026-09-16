package com.ecommerce.cart.application.dto;

import com.ecommerce.cart.domain.entity.Cart;
import com.ecommerce.cart.domain.entity.CartItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    private String customerId;
    private List<CartItem> items;
    private BigDecimal totalAmount;
    private int totalQuantity;

    public static CartResponse fromDomain(Cart cart) {
        return CartResponse.builder()
                .customerId(cart.getCustomerId())
                .items(cart.getItems())
                .totalAmount(cart.getTotalAmount())
                .totalQuantity(cart.getTotalQuantity())
                .build();
    }
}
