package com.ecommerce.inventory.domain.entity;

import java.time.Instant;

/**
 * Domain Entity for Inventory (Sổ cái tồn kho Invariant - Bảng 11).
 * Invariant rule: available_stock + reserved_stock + sold_stock == total_stock
 */
public class Inventory {

    private Long id;
    private String productId;
    private Integer totalStock;
    private Integer availableStock;
    private Integer reservedStock;
    private Integer soldStock;
    private Long version;
    private Instant updatedAt;

    // Private constructor
    private Inventory() {
    }

    public static Inventory create(String productId, Integer quantity, Integer reservedStock) {
        Inventory inventory = new Inventory();
        inventory.productId = productId;
        int avail = quantity != null ? quantity : 0;
        int reserved = reservedStock != null ? reservedStock : 0;
        inventory.availableStock = avail;
        inventory.reservedStock = reserved;
        inventory.soldStock = 0;
        inventory.totalStock = avail + reserved;
        inventory.updatedAt = Instant.now();
        return inventory;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Kiểm tra tính toàn vẹn bất biến của sổ cái tồn kho (Mục 4.2.3):
     * available_stock + reserved_stock + sold_stock == total_stock
     */
    public boolean validateInvariant() {
        int avail = availableStock != null ? availableStock : 0;
        int res = reservedStock != null ? reservedStock : 0;
        int sold = soldStock != null ? soldStock : 0;
        int total = totalStock != null ? totalStock : 0;
        return (avail + res + sold) == total;
    }

    // Business Logic Methods
    public boolean reserve(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Reservation amount must be greater than zero");
        }
        int avail = availableStock != null ? availableStock : 0;
        if (avail >= amount) {
            this.availableStock = avail - amount;
            this.reservedStock = (this.reservedStock != null ? this.reservedStock : 0) + amount;
            this.updatedAt = Instant.now();
            return true;
        }
        return false; // Out of stock
    }

    public void restore(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Restore amount must be greater than zero");
        }
        int res = this.reservedStock != null ? this.reservedStock : 0;
        int toRestore = Math.min(amount, res);
        this.reservedStock = res - toRestore;
        this.availableStock = (this.availableStock != null ? this.availableStock : 0) + toRestore;
        this.updatedAt = Instant.now();
    }

    public boolean confirmSold(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Sold amount must be greater than zero");
        }
        int res = this.reservedStock != null ? this.reservedStock : 0;
        if (res >= amount) {
            this.reservedStock = res - amount;
            this.soldStock = (this.soldStock != null ? this.soldStock : 0) + amount;
            this.updatedAt = Instant.now();
            return true;
        }
        return false;
    }

    public void updateStock(int newQuantity) {
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        int currentReserved = this.reservedStock != null ? this.reservedStock : 0;
        int currentSold = this.soldStock != null ? this.soldStock : 0;
        this.availableStock = newQuantity;
        this.totalStock = newQuantity + currentReserved + currentSold;
        this.updatedAt = Instant.now();
    }

    public boolean hasStock(int amount) {
        int avail = availableStock != null ? availableStock : 0;
        return avail >= amount;
    }

    // Backward compatibility getters
    public Integer getQuantity() {
        return availableStock;
    }

    public Integer getReservedQuantity() {
        return reservedStock;
    }

    // Getters
    public Long getId() { return id; }
    public String getProductId() { return productId; }
    public Integer getTotalStock() { return totalStock; }
    public Integer getAvailableStock() { return availableStock; }
    public Integer getReservedStock() { return reservedStock; }
    public Integer getSoldStock() { return soldStock; }
    public Long getVersion() { return version; }
    public Instant getUpdatedAt() { return updatedAt; }

    public static class Builder {
        private final Inventory inventory = new Inventory();

        public Builder id(Long id) { inventory.id = id; return this; }
        public Builder productId(String productId) { inventory.productId = productId; return this; }
        public Builder totalStock(Integer totalStock) { inventory.totalStock = totalStock; return this; }
        public Builder availableStock(Integer availableStock) { inventory.availableStock = availableStock; return this; }
        public Builder reservedStock(Integer reservedStock) { inventory.reservedStock = reservedStock; return this; }
        public Builder soldStock(Integer soldStock) { inventory.soldStock = soldStock; return this; }
        public Builder quantity(Integer quantity) {
            inventory.availableStock = quantity;
            return this;
        }
        public Builder reservedQuantity(Integer reservedQuantity) {
            inventory.reservedStock = reservedQuantity;
            return this;
        }
        public Builder version(Long version) { inventory.version = version; return this; }
        public Builder updatedAt(Instant updatedAt) { inventory.updatedAt = updatedAt; return this; }

        public Inventory build() {
            if (inventory.availableStock == null) {
                inventory.availableStock = 0;
            }
            if (inventory.reservedStock == null) {
                inventory.reservedStock = 0;
            }
            if (inventory.soldStock == null) {
                inventory.soldStock = 0;
            }
            if (inventory.totalStock == null) {
                inventory.totalStock = inventory.availableStock + inventory.reservedStock + inventory.soldStock;
            }
            return inventory;
        }
    }
}
