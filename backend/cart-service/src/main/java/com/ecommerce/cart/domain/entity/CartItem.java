package com.ecommerce.cart.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CartItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private String productId;
    private String productTitle;
    private String imageUrl;
    private Integer quantity;
    private BigDecimal unitPrice;

    @JsonProperty("subtotal")
    public BigDecimal getSubtotal() {
        if (unitPrice == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    @JsonProperty("subtotal")
    public void setSubtotal(BigDecimal subtotal) {
        // No-op for Jackson deserialization compatibility
    }
}
