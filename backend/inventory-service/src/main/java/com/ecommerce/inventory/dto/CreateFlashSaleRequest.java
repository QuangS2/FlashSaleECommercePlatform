package com.ecommerce.inventory.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateFlashSaleRequest {

    @NotNull(message = "Thời gian bắt đầu không được để trống")
    private Instant startTime;

    @NotNull(message = "Thời gian kết thúc không được để trống")
    private Instant endTime;

    private List<FlashSaleItemDto> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FlashSaleItemDto {
        private String productId;
        private BigDecimal originalPrice;
        private BigDecimal flashPrice;
        private Integer allocatedStock;
        private Integer userLimit;
    }
}
