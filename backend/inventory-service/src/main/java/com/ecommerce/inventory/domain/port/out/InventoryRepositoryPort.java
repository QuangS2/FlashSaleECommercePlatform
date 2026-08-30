package com.ecommerce.inventory.domain.port.out;

import com.ecommerce.inventory.domain.entity.Inventory;

import java.util.Optional;

/**
 * Outbound Port for Inventory Repository.
 */
public interface InventoryRepositoryPort {

    Inventory save(Inventory inventory);

    Optional<Inventory> findByProductId(String productId);
}
