package com.ecommerce.product.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private String id;
    private String name;
    private String category;
    private String description;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private Integer discountPercent;
    private String imageUrl;
    private Double rating;
    private Integer soldCount;
    private Integer stockCount;
    private Map<String, String> specs;
    private Boolean isFlashSale;

    // Domain Logic: calculate final price if discount exists
    public BigDecimal calculateFinalPrice() {
        if (discountPrice != null && discountPrice.compareTo(BigDecimal.ZERO) > 0) {
            return price.subtract(discountPrice);
        }
        return price;
    }
}
