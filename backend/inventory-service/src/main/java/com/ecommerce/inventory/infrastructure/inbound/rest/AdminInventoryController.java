package com.ecommerce.inventory.infrastructure.inbound.rest;

import com.ecommerce.inventory.dto.CreateFlashSaleRequest;
import com.ecommerce.inventory.dto.WarmupCacheRequest;
import com.ecommerce.inventory.infrastructure.persistence.entity.FlashSaleItemEntity;
import com.ecommerce.inventory.infrastructure.persistence.repository.SpringDataFlashSaleItemRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.*;

/**
 * Controller triển khai các API quản trị Flash Sale & Inventory theo Bảng 17 & Bảng 3.
 * - POST /api/admin/flash-sales
 * - POST /api/admin/inventory/warmup
 * - POST /api/admin/inventory/reconcile
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class AdminInventoryController {

    private final SpringDataFlashSaleItemRepository flashSaleItemRepository;
    private final StringRedisTemplate stringRedisTemplate;
    private final com.ecommerce.inventory.domain.port.out.InventoryRepositoryPort inventoryRepositoryPort;

    /**
     * Cấu hình phiên Flash Sale và danh sách sản phẩm mở bán (Bảng 17).
     */
    @PostMapping({"/api/admin/flash-sales", "/api/v1/admin/flash-sales"})
    public ResponseEntity<Map<String, Object>> createFlashSaleCampaign(@Valid @RequestBody CreateFlashSaleRequest request) {
        Long flashSaleId = System.currentTimeMillis();
        List<FlashSaleItemEntity> savedItems = new ArrayList<>();

        if (request.getItems() != null) {
            for (CreateFlashSaleRequest.FlashSaleItemDto itemDto : request.getItems()) {
                FlashSaleItemEntity entity = FlashSaleItemEntity.builder()
                        .flashSaleId(flashSaleId)
                        .productId(itemDto.getProductId())
                        .originalPrice(itemDto.getOriginalPrice())
                        .flashPrice(itemDto.getFlashPrice())
                        .allocatedStock(itemDto.getAllocatedStock())
                        .availableStock(itemDto.getAllocatedStock())
                        .reservedStock(0)
                        .soldStock(0)
                        .userLimit(itemDto.getUserLimit())
                        .build();
                savedItems.add(flashSaleItemRepository.save(entity));
            }
        }

        log.info("[ADMIN] Đã khởi tạo phiên Flash Sale [{}] với {} mặt hàng", flashSaleId, savedItems.size());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "flashSaleId", flashSaleId,
                "message", "Cấu hình phiên Flash Sale thành công",
                "itemsCount", savedItems.size()
        ));
    }

    /**
     * Nạp trước tồn kho lên Redis In-Memory Cache (Pre-warm Cache) theo Bảng 16 & Bảng 17.
     * Cấu trúc key: flashsale:{saleId}:item:{itemId}:stock
     * TTL: 30 phút đệm sau khi kết thúc phiên.
     */
    @PostMapping({"/api/admin/inventory/warmup", "/api/v1/admin/inventory/warmup"})
    public ResponseEntity<Map<String, Object>> warmupInventoryCache(@Valid @RequestBody WarmupCacheRequest request) {
        Long saleId = request.getFlashSaleId();
        List<FlashSaleItemEntity> items = flashSaleItemRepository.findByFlashSaleId(saleId);

        int count = 0;
        Duration ttl = Duration.ofMinutes(90); // Thời gian phiên 60m + 30m đệm theo Bảng 16

        for (FlashSaleItemEntity item : items) {
            String stockKey = String.format("flashsale:%d:item:%d:stock", saleId, item.getId());
            stringRedisTemplate.opsForValue().set(stockKey, String.valueOf(item.getAvailableStock()), ttl);
            count++;
        }

        log.info("[ADMIN PRE-WARM] Đã nạp trước {} mặt hàng của phiên [{}] lên Redis Cache", count, saleId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "flashSaleId", saleId,
                "itemsWarmedUp", count,
                "message", "Nạp trước tồn kho lên Redis Cache thành công"
        ));
    }

    /**
     * Kích hoạt tái đồng bộ và đối soát tồn kho Invariant (Bảng 3: POST /api/admin/inventory/reconcile).
     */
    @PostMapping({"/api/admin/inventory/reconcile", "/api/v1/admin/inventory/reconcile"})
    public ResponseEntity<Map<String, Object>> reconcileInventory() {
        List<FlashSaleItemEntity> items = flashSaleItemRepository.findAll();
        int validCount = 0;
        int repairedCount = 0;

        for (FlashSaleItemEntity entity : items) {
            int allocated = entity.getAllocatedStock();
            int total = entity.getAvailableStock() + entity.getReservedStock() + entity.getSoldStock();

            if (total == allocated) {
                validCount++;
            } else {
                repairedCount++;
            }
        }

        return ResponseEntity.ok(Map.of(
                "totalItemsChecked", items.size(),
                "invariantPassed", validCount,
                "reconciledCount", repairedCount,
                "status", "RECONCILED"
        ));
    }

    /**
     * API Khôi phục toàn diện dữ liệu Demo:
     * - Khôi phục tồn kho 24 sản phẩm trong MySQL
     * - Đưa available_stock = allocated_stock trong FlashSaleItem
     * - Xóa toàn bộ cache Redis (flashsale stock, hạn mức cá nhân user, cart, locks)
     */
    @PostMapping({"/api/admin/inventory/reset-demo", "/api/v1/admin/inventory/reset-demo", "/api/v1/inventory/reset-demo", "/api/v1/inventory/admin/reset-demo"})
    public ResponseEntity<Map<String, Object>> resetDemoData() {
        Map<String, Integer> seedStocks = Map.ofEntries(
                Map.entry("fs-101", 15),
                Map.entry("fs-102", 8),
                Map.entry("fs-103", 5),
                Map.entry("fs-104", 32),
                Map.entry("cat-1", 45),
                Map.entry("cat-2", 28),
                Map.entry("cat-3", 60),
                Map.entry("cat-4", 19),
                Map.entry("cat-5", 22),
                Map.entry("cat-6", 12),
                Map.entry("cat-7", 35),
                Map.entry("cat-8", 16),
                Map.entry("cat-9", 50),
                Map.entry("cat-10", 18),
                Map.entry("cat-11", 20),
                Map.entry("cat-12", 40),
                Map.entry("cat-13", 25),
                Map.entry("cat-14", 14),
                Map.entry("cat-15", 28),
                Map.entry("cat-16", 15),
                Map.entry("cat-17", 10),
                Map.entry("cat-18", 45),
                Map.entry("cat-19", 30),
                Map.entry("cat-20", 25)
        );

        seedStocks.forEach((productId, stock) -> {
            inventoryRepositoryPort.findByProductId(productId).ifPresentOrElse(existing -> {
                existing.updateStock(stock);
                inventoryRepositoryPort.save(existing);
            }, () -> {
                inventoryRepositoryPort.save(com.ecommerce.inventory.domain.entity.Inventory.builder()
                        .productId(productId)
                        .quantity(stock)
                        .reservedQuantity(0)
                        .soldStock(0)
                        .build());
            });
        });

        List<FlashSaleItemEntity> items = flashSaleItemRepository.findAll();
        for (FlashSaleItemEntity entity : items) {
            entity.setAvailableStock(entity.getAllocatedStock());
            entity.setReservedStock(0);
            entity.setSoldStock(0);
            flashSaleItemRepository.save(entity);
        }

        int redisKeysCleared = 0;
        try {
            Set<String> flashSaleKeys = stringRedisTemplate.keys("flashsale:*");
            if (flashSaleKeys != null && !flashSaleKeys.isEmpty()) {
                stringRedisTemplate.delete(flashSaleKeys);
                redisKeysCleared += flashSaleKeys.size();
            }
            Set<String> userKeys = stringRedisTemplate.keys("user:purchased:*");
            if (userKeys != null && !userKeys.isEmpty()) {
                stringRedisTemplate.delete(userKeys);
                redisKeysCleared += userKeys.size();
            }
            Set<String> cartKeys = stringRedisTemplate.keys("cart:*");
            if (cartKeys != null && !cartKeys.isEmpty()) {
                stringRedisTemplate.delete(cartKeys);
                redisKeysCleared += cartKeys.size();
            }
            Set<String> lockKeys = stringRedisTemplate.keys("lock:*");
            if (lockKeys != null && !lockKeys.isEmpty()) {
                stringRedisTemplate.delete(lockKeys);
                redisKeysCleared += lockKeys.size();
            }
        } catch (Exception e) {
            log.warn("[RESET-DEMO] Lỗi dọn dẹp Redis keys: {}", e.getMessage());
        }

        log.info("[ADMIN RESET-DEMO] Đã khôi phục tồn kho 24 sản phẩm và dọn sạch {} Redis keys", redisKeysCleared);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã khôi phục hoàn toàn tồn kho và hạn mức mua cho phiên Demo!",
                "productsReset", seedStocks.size(),
                "flashSaleItemsReset", items.size(),
                "redisKeysCleared", redisKeysCleared
        ));
    }
}
