package com.ecommerce.product.application.dto;

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
public class CreateProductRequest {
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
}
