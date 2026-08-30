package com.ecommerce.product.domain.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ProductTest {

    @Test
    void testProductCreation() {
        Product product = Product.builder()
                .id("prod_1")
                .name("Laptop")
                .description("Gaming Laptop")
                .price(new BigDecimal("1500.00"))
                .discountPrice(new BigDecimal("1400.00"))
                .imageUrl("http://example.com/laptop.jpg")
                .build();

        assertNotNull(product);
        assertEquals("prod_1", product.getId());
        assertEquals("Laptop", product.getName());
        assertEquals("Gaming Laptop", product.getDescription());
        assertEquals(new BigDecimal("1500.00"), product.getPrice());
        assertEquals(new BigDecimal("1400.00"), product.getDiscountPrice());
        assertEquals("http://example.com/laptop.jpg", product.getImageUrl());
    }

}
