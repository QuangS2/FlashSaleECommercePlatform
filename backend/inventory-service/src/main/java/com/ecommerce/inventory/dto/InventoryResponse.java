package com.ecommerce.inventory.dto;

import com.ecommerce.inventory.domain.entity.Inventory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryResponse {

    private String productId;
    private Integer availableQuantity;
    private Integer reservedQuantity;
    private Instant updatedAt;
    private String message;

    public static InventoryResponse fromEntity(Inventory inventory, String message) {
        return InventoryResponse.builder()
                .productId(inventory.getProductId())
                .availableQuantity(inventory.getQuantity())
                .reservedQuantity(inventory.getReservedQuantity() != null ? inventory.getReservedQuantity() : 0)
                .updatedAt(inventory.getUpdatedAt())
                .message(message)
                .build();
    }
}
