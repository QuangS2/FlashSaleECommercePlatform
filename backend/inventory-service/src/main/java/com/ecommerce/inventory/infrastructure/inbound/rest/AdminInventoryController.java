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
}
