package com.ecommerce.inventory.controller;

import com.ecommerce.inventory.model.Inventory;
import com.ecommerce.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/{productId}")
    public ResponseEntity<Inventory> getInventoryByProductId(@PathVariable String productId) {
        return inventoryService.getInventoryByProductId(productId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Inventory> saveInventory(@RequestBody Inventory inventory) {
        return ResponseEntity.ok(inventoryService.saveInventory(inventory));
    }

    @GetMapping("/{productId}/check")
    public ResponseEntity<Boolean> isInStock(@PathVariable String productId, @RequestParam int quantity) {
        return ResponseEntity.ok(inventoryService.isInStock(productId, quantity));
    }

    @PostMapping("/deduct")
    public ResponseEntity<Boolean> deductInventory(@RequestParam String productId, @RequestParam int quantity) {
        try {
            boolean success = inventoryService.deductInventory(productId, quantity);
            if (success) {
                return ResponseEntity.ok(true);
            }
            return ResponseEntity.badRequest().body(false); // Hoặc 400 Bad Request nếu không đủ hàng
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(false);
        }
    }
}
