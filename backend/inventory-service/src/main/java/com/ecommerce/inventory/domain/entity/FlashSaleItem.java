package com.ecommerce.inventory.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Domain entity representing an item in a Flash Sale campaign.
 * Corresponds to Bảng 12 (flash_sale_items) in Graduation Project Report.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashSaleItem {

    private Long id;
    private Long flashSaleId;
    private String productId;
    private BigDecimal originalPrice;
    private BigDecimal flashPrice;
    private Integer allocatedStock;
    private Integer availableStock;
    private Integer reservedStock;
    private Integer soldStock;
    private Integer userLimit;
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * Sổ cái tồn kho Invariant (Mục 4.2.3 & Bảng 12):
     * available_stock + reserved_stock + sold_stock == allocated_stock
     */
    public boolean validateInvariant() {
        int avail = availableStock != null ? availableStock : 0;
        int reserved = reservedStock != null ? reservedStock : 0;
        int sold = soldStock != null ? soldStock : 0;
        int allocated = allocatedStock != null ? allocatedStock : 0;
        return (avail + reserved + sold) == allocated;
    }

    public boolean reserve(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Số lượng giữ chỗ phải lớn hơn 0");
        }
        int avail = availableStock != null ? availableStock : 0;
        if (avail >= amount) {
            this.availableStock = avail - amount;
            this.reservedStock = (this.reservedStock != null ? this.reservedStock : 0) + amount;
            this.updatedAt = Instant.now();
            return true;
        }
        return false;
    }

    public boolean confirmSold(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Số lượng bán phải lớn hơn 0");
        }
        int reserved = reservedStock != null ? reservedStock : 0;
        if (reserved >= amount) {
            this.reservedStock = reserved - amount;
            this.soldStock = (this.soldStock != null ? this.soldStock : 0) + amount;
            this.updatedAt = Instant.now();
            return true;
        }
        return false;
    }

    public void releaseReservation(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Số lượng hoàn trả phải lớn hơn 0");
        }
        int reserved = reservedStock != null ? reservedStock : 0;
        int toRelease = Math.min(amount, reserved);
        this.reservedStock = reserved - toRelease;
        this.availableStock = (this.availableStock != null ? this.availableStock : 0) + toRelease;
        this.updatedAt = Instant.now();
    }
}
