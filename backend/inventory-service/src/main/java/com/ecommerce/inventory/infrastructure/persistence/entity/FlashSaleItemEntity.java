package com.ecommerce.inventory.infrastructure.persistence.entity;

import com.ecommerce.inventory.domain.entity.FlashSaleItem;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "flash_sale_items", indexes = {
        @Index(name = "idx_flash_sale_id", columnList = "flash_sale_id"),
        @Index(name = "idx_flash_sale_product_id", columnList = "product_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlashSaleItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "flash_sale_id", nullable = false)
    private Long flashSaleId;

    @Column(name = "product_id", nullable = false, length = 64)
    private String productId;

    @Column(name = "original_price", precision = 12, scale = 2)
    private BigDecimal originalPrice;

    @Column(name = "flash_price", precision = 12, scale = 2)
    private BigDecimal flashPrice;

    @Column(name = "allocated_stock", nullable = false)
    private Integer allocatedStock;

    @Column(name = "available_stock", nullable = false)
    private Integer availableStock;

    @Builder.Default
    @Column(name = "reserved_stock", nullable = false)
    private Integer reservedStock = 0;

    @Builder.Default
    @Column(name = "sold_stock", nullable = false)
    private Integer soldStock = 0;

    @Builder.Default
    @Column(name = "user_limit", nullable = false)
    private Integer userLimit = 1;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        if (this.reservedStock == null) this.reservedStock = 0;
        if (this.soldStock == null) this.soldStock = 0;
        if (this.userLimit == null) this.userLimit = 1;
        if (this.availableStock == null && this.allocatedStock != null) {
            this.availableStock = this.allocatedStock;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public static FlashSaleItemEntity fromDomain(FlashSaleItem item) {
        return FlashSaleItemEntity.builder()
                .id(item.getId())
                .flashSaleId(item.getFlashSaleId())
                .productId(item.getProductId())
                .originalPrice(item.getOriginalPrice())
                .flashPrice(item.getFlashPrice())
                .allocatedStock(item.getAllocatedStock())
                .availableStock(item.getAvailableStock())
                .reservedStock(item.getReservedStock())
                .soldStock(item.getSoldStock())
                .userLimit(item.getUserLimit())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    public FlashSaleItem toDomain() {
        return FlashSaleItem.builder()
                .id(this.id)
                .flashSaleId(this.flashSaleId)
                .productId(this.productId)
                .originalPrice(this.originalPrice)
                .flashPrice(this.flashPrice)
                .allocatedStock(this.allocatedStock)
                .availableStock(this.availableStock)
                .reservedStock(this.reservedStock)
                .soldStock(this.soldStock)
                .userLimit(this.userLimit)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
}
