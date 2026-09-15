package com.ecommerce.cart.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cart implements Serializable {
    private static final long serialVersionUID = 1L;

    private String customerId;
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    public BigDecimal getTotalAmount() {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getTotalQuantity() {
        if (items == null || items.isEmpty()) {
            return 0;
        }
        return items.stream()
                .mapToInt(item -> item.getQuantity() != null ? item.getQuantity() : 0)
                .sum();
    }

    public void addItem(CartItem newItem) {
        if (items == null) {
            items = new ArrayList<>();
        }
        for (CartItem existing : items) {
            if (existing.getProductId().equals(newItem.getProductId())) {
                existing.setQuantity(existing.getQuantity() + newItem.getQuantity());
                return;
            }
        }
        items.add(newItem);
    }

    public void removeItem(String productId) {
        if (items != null) {
            items.removeIf(item -> item.getProductId().equals(productId));
        }
    }

    public void clear() {
        if (items != null) {
            items.clear();
        }
    }
}
