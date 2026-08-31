package com.ecommerce.inventory.domain.entity;

import java.time.Instant;

/**
 * Domain Entity for Inventory.
 * Pure Java object, completely decoupled from JPA/Spring.
 */
public class Inventory {

    private Long id; // Surrogate key (optional)
    private String productId; // Business Key
    private Integer quantity;
    private Integer reservedQuantity;
    private Long version; // Optimistic locking
    private Instant updatedAt;

    // Private constructor
    private Inventory() {
    }

    public static Inventory create(String productId, Integer quantity, Integer reservedQuantity) {
        Inventory inventory = new Inventory();
        inventory.productId = productId;
        inventory.quantity = quantity != null ? quantity : 0;
        inventory.reservedQuantity = reservedQuantity != null ? reservedQuantity : 0;
        inventory.updatedAt = Instant.now();
        return inventory;
    }

    public static Builder builder() {
        return new Builder();
    }

    // Business Logic Methods
    public boolean reserve(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Reservation amount must be greater than zero");
        }
        if (this.quantity >= amount) {
            this.quantity -= amount;
            this.reservedQuantity += amount;
            this.updatedAt = Instant.now();
            return true;
        }
        return false; // Out of stock
    }

    public void restore(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Restore amount must be greater than zero");
        }
        this.quantity += amount;
        if (this.reservedQuantity >= amount) {
            this.reservedQuantity -= amount;
        }
        this.updatedAt = Instant.now();
    }

    public void updateStock(int newQuantity) {
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        this.quantity = newQuantity;
        this.updatedAt = Instant.now();
    }

    public boolean hasStock(int amount) {
        return this.quantity >= amount;
    }

    // Getters
    public Long getId() { return id; }
    public String getProductId() { return productId; }
    public Integer getQuantity() { return quantity; }
    public Integer getReservedQuantity() { return reservedQuantity; }
    public Long getVersion() { return version; }
    public Instant getUpdatedAt() { return updatedAt; }

    public static class Builder {
        private final Inventory inventory = new Inventory();

        public Builder id(Long id) { inventory.id = id; return this; }
        public Builder productId(String productId) { inventory.productId = productId; return this; }
        public Builder quantity(Integer quantity) { inventory.quantity = quantity; return this; }
        public Builder reservedQuantity(Integer reservedQuantity) { inventory.reservedQuantity = reservedQuantity; return this; }
        public Builder version(Long version) { inventory.version = version; return this; }
        public Builder updatedAt(Instant updatedAt) { inventory.updatedAt = updatedAt; return this; }
        public Inventory build() { return inventory; }
    }
}
