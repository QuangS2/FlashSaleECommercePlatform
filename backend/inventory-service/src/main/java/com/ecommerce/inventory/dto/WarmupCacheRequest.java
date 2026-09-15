package com.ecommerce.inventory.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarmupCacheRequest {

    @NotNull(message = "Flash sale ID không được để trống")
    private Long flashSaleId;
}
