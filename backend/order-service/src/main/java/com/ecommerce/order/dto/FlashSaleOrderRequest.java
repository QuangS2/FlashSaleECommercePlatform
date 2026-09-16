package com.ecommerce.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashSaleOrderRequest {

    @NotNull(message = "Flash sale ID không được để trống")
    private Long saleId;

    @NotNull(message = "Item ID không được để trống")
    private Long itemId;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng mua tối thiểu là 1")
    private Integer quantity;

    private String idempotencyKey;

    private BigDecimal unitPrice;

    private String userEmail;
}
