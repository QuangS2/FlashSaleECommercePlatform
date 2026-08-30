package com.ecommerce.inventory.infrastructure.inbound.rest;

import com.ecommerce.inventory.application.port.in.InventoryUseCase;
import com.ecommerce.inventory.dto.InventoryResponse;
import com.ecommerce.inventory.dto.UpdateStockRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryUseCase inventoryUseCase;

    /**
     * Tra cứu thông tin tồn kho của sản phẩm.
     */
    @GetMapping("/{productId}")
    public ResponseEntity<InventoryResponse> getInventoryByProductId(@PathVariable String productId) {
        InventoryResponse response = inventoryUseCase.getInventoryByProductId(productId);
        return ResponseEntity.ok(response);
    }

    /**
     * Cập nhật / nạp thêm số lượng tồn kho.
     */
    @PostMapping("/stock")
    public ResponseEntity<InventoryResponse> updateStock(@Valid @RequestBody UpdateStockRequest request) {
        InventoryResponse response = inventoryUseCase.updateStock(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Kiểm tra nhanh sản phẩm còn đủ hàng không.
     */
    @GetMapping("/{productId}/check")
    public ResponseEntity<Boolean> isInStock(@PathVariable String productId, @RequestParam int quantity) {
        return ResponseEntity.ok(inventoryUseCase.isInStock(productId, quantity));
    }

    /**
     * Endpoint kiểm tra trạng thái sức khỏe service.
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "inventory-service",
                "architecture", "Hexagonal Architecture",
                "concurrencyControl", "Redisson Distributed Lock via Port Adapter",
                "database", "MySQL 8.0"
        ));
    }
}
