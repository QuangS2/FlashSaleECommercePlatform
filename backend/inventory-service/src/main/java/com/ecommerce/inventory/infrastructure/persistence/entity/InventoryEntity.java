package com.ecommerce.inventory.infrastructure.persistence.entity;

import com.ecommerce.inventory.domain.entity.Inventory;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "inventory", indexes = {
        @Index(name = "idx_inventory_product_id", columnList = "product_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false, unique = true, length = 64)
    private String productId;

    @Builder.Default
    @Column(name = "total_stock", nullable = false)
    private Integer totalStock = 0;

    @Builder.Default
    @Column(name = "available_stock", nullable = false)
    private Integer availableStock = 0;

    @Builder.Default
    @Column(name = "reserved_stock", nullable = false)
    private Integer reservedStock = 0;

    @Builder.Default
    @Column(name = "sold_stock", nullable = false)
    private Integer soldStock = 0;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
        if (this.totalStock == null || this.totalStock == 0) {
            int avail = this.availableStock != null ? this.availableStock : 0;
            int res = this.reservedStock != null ? this.reservedStock : 0;
            int sold = this.soldStock != null ? this.soldStock : 0;
            this.totalStock = avail + res + sold;
        }
    }

    // Tương thích ngược với quantity / reservedQuantity
    public Integer getQuantity() {
        return availableStock;
    }

    public void setQuantity(Integer quantity) {
        this.availableStock = quantity;
    }

    public Integer getReservedQuantity() {
        return reservedStock;
    }

    public void setReservedQuantity(Integer reservedQuantity) {
        this.reservedStock = reservedQuantity;
    }

    public static InventoryEntity fromDomain(Inventory inventory) {
        return InventoryEntity.builder()
                .id(inventory.getId())
                .productId(inventory.getProductId())
                .totalStock(inventory.getTotalStock())
                .availableStock(inventory.getAvailableStock())
                .reservedStock(inventory.getReservedStock())
                .soldStock(inventory.getSoldStock())
                .version(inventory.getVersion())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }

    public Inventory toDomain() {
        return Inventory.builder()
                .id(this.id)
                .productId(this.productId)
                .totalStock(this.totalStock)
                .availableStock(this.availableStock)
                .reservedStock(this.reservedStock)
                .soldStock(this.soldStock)
                .version(this.version)
                .updatedAt(this.updatedAt)
                .build();
    }
}
