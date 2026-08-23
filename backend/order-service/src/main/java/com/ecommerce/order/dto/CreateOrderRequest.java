package com.ecommerce.order.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {

    @NotBlank(message = "userId không được để trống")
    private String userId;

    private String userEmail;

    @NotBlank(message = "productId không được để trống")
    private String productId;

    private String productTitle;

    @NotNull(message = "quantity không được null")
    @Min(value = 1, message = "Số lượng mua tối thiểu là 1")
    private Integer quantity;

    @NotNull(message = "unitPrice không được null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Đơn giá phải lớn hơn 0")
    private BigDecimal unitPrice;
}
