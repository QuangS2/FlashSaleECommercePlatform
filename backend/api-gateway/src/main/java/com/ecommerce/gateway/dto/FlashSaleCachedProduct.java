package com.ecommerce.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlashSaleCachedProduct {

    private String productId;
    private String title;
    private BigDecimal originalPrice;
    private BigDecimal flashSalePrice;
    private int discountPercentage;
    private String stockStatus;
    @Builder.Default
    private boolean cached = true;
}
