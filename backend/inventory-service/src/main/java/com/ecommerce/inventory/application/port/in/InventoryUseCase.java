package com.ecommerce.inventory.application.port.in;

import com.ecommerce.inventory.dto.InventoryResponse;
import com.ecommerce.inventory.dto.UpdateStockRequest;

/**
 * Inbound Port for Inventory Use Cases.
 */
public interface InventoryUseCase {

    boolean reserveStock(String orderId, String productId, int quantity);

    boolean restoreStock(String orderId, String productId, int quantity, String reason);

    InventoryResponse getInventoryByProductId(String productId);

    InventoryResponse updateStock(UpdateStockRequest request);

    boolean isInStock(String productId, int quantity);

    boolean deductInventory(String productId, int quantity);
}
