package com.ecommerce.inventory.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateStockRequest {

    @NotBlank(message = "productId không được để trống")
    private String productId;

    @NotNull(message = "quantity không được null")
    @Min(value = 0, message = "Số lượng tồn kho không được âm")
    private Integer quantity;
}
