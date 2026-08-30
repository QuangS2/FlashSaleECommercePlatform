package com.ecommerce.inventory.application.service;

import com.ecommerce.common.event.BaseEvent;
import com.ecommerce.common.event.EventType;
import com.ecommerce.common.event.inventory.InventoryReservationFailedEvent;
import com.ecommerce.common.event.inventory.InventoryReservedEvent;
import com.ecommerce.common.event.inventory.InventoryRestoredEvent;
import com.ecommerce.inventory.application.port.in.InventoryUseCase;
import com.ecommerce.inventory.domain.entity.Inventory;
import com.ecommerce.inventory.domain.port.out.DistributedLockPort;
import com.ecommerce.inventory.domain.port.out.EventPublisherPort;
import com.ecommerce.inventory.domain.port.out.InventoryRepositoryPort;
import com.ecommerce.inventory.dto.InventoryResponse;
import com.ecommerce.inventory.dto.UpdateStockRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

/**
 * Application Service for Inventory.
 * Orchestrates domain logic, repositories, locking, and events.
 */
@Service
@RequiredArgsConstructor
public class InventoryApplicationService implements InventoryUseCase {

    private static final Logger log = LoggerFactory.getLogger(InventoryApplicationService.class);

    private final InventoryRepositoryPort inventoryRepositoryPort;
    private final DistributedLockPort distributedLockPort;
    private final EventPublisherPort eventPublisherPort;

    @Override
    public boolean reserveStock(String orderId, String productId, int quantity) {
        String lockKey = "inventory:lock:" + productId;

        try {
            Boolean success = distributedLockPort.executeWithLock(lockKey, 5, 5, () -> {
                Optional<Inventory> optInv = inventoryRepositoryPort.findByProductId(productId);
                
                if (optInv.isPresent()) {
                    Inventory inventory = optInv.get();
                    boolean reserved = inventory.reserve(quantity);
                    
                    if (reserved) {
                        inventoryRepositoryPort.save(inventory);
                        log.info("[INVENTORY SUCCESS] Đã giữ chỗ thành công {} sản phẩm [{}] cho đơn hàng [{}]. Tồn kho còn lại: {}",
                                quantity, productId, orderId, inventory.getQuantity());

                        InventoryReservedEvent payload = InventoryReservedEvent.builder()
                                .orderId(orderId)
                                .productId(productId)
                                .quantityReserved(quantity)
                                .remainingStock(inventory.getQuantity())
                                .status("SUCCESS")
                                .reservedAt(Instant.now())
                                .build();

                        BaseEvent<InventoryReservedEvent> event = BaseEvent.of(
                                EventType.INVENTORY_RESERVED,
                                "CORR-" + orderId,
                                "inventory-service",
                                payload
                        );
                        eventPublisherPort.publishInventoryReservedEvent(orderId, event);
                        return true;
                    }
                }
                
                int available = optInv.map(Inventory::getQuantity).orElse(0);
                log.warn("[INVENTORY OUT_OF_STOCK] Sản phẩm [{}] không đủ hàng (Yêu cầu: {}, Tồn kho: {}) cho đơn [{}]",
                        productId, quantity, available, orderId);

                publishReservationFailed(orderId, productId, quantity, "Sản phẩm đã hết hàng tồn kho Flash Sale");
                return false;
            });

            if (success == null) {
                // Lock acquisition failed (timeout)
                log.warn("[INVENTORY LOCK TIMEOUT] Không thể lấy lock cho sản phẩm [{}] đơn [{}]", productId, orderId);
                publishReservationFailed(orderId, productId, quantity, "Hệ thống đang quá tải, không thể lấy khóa kho");
                return false;
            }
            return success;

        } catch (Exception e) {
            log.error("[INVENTORY ERROR] Lỗi khi xử lý kho cho sản phẩm [{}]: {}", productId, e.getMessage());
            publishReservationFailed(orderId, productId, quantity, "Lỗi hệ thống khi xử lý tồn kho");
            return false;
        }
    }

    @Override
    public boolean restoreStock(String orderId, String productId, int quantity, String reason) {
        String lockKey = "inventory:lock:" + productId;

        try {
            Boolean success = distributedLockPort.executeWithLock(lockKey, 5, 5, () -> {
                Optional<Inventory> optInv = inventoryRepositoryPort.findByProductId(productId);
                if (optInv.isPresent()) {
                    Inventory inventory = optInv.get();
                    inventory.restore(quantity);
                    inventoryRepositoryPort.save(inventory);

                    log.info("[INVENTORY COMPENSATED] Đã hoàn trả {} sản phẩm [{}] cho đơn [{}] (Lý do: {}). Tồn kho mới: {}",
                            quantity, productId, orderId, reason, inventory.getQuantity());

                    InventoryRestoredEvent payload = InventoryRestoredEvent.builder()
                            .orderId(orderId)
                            .productId(productId)
                            .quantityRestored(quantity)
                            .updatedStock(inventory.getQuantity())
                            .reason(reason)
                            .restoredAt(Instant.now())
                            .build();

                    BaseEvent<InventoryRestoredEvent> event = BaseEvent.of(
                            EventType.INVENTORY_RESTORED,
                            "CORR-" + orderId,
                            "inventory-service",
                            payload
                    );

                    eventPublisherPort.publishInventoryRestoredEvent(orderId, event);
                    return true;
                }
                return false;
            });

            if (success == null) {
                log.warn("[INVENTORY RESTORE TIMEOUT] Không thể lấy lock hoàn kho cho sản phẩm [{}] đơn [{}]", productId, orderId);
                return false;
            }
            return success;

        } catch (Exception e) {
            log.error("[INVENTORY RESTORE ERROR] Lỗi khi hoàn kho cho sản phẩm [{}]: {}", productId, e.getMessage());
            return false;
        }
    }

    private void publishReservationFailed(String orderId, String productId, int quantity, String failureReason) {
        InventoryReservationFailedEvent payload = InventoryReservationFailedEvent.builder()
                .orderId(orderId)
                .productId(productId)
                .requestedQuantity(quantity)
                .failureReason(failureReason)
                .failedAt(Instant.now())
                .build();

        BaseEvent<InventoryReservationFailedEvent> event = BaseEvent.of(
                EventType.INVENTORY_RESERVATION_FAILED,
                "CORR-" + orderId,
                "inventory-service",
                payload
        );

        eventPublisherPort.publishInventoryReservationFailedEvent(orderId, event);
    }

    @Override
    public InventoryResponse getInventoryByProductId(String productId) {
        return inventoryRepositoryPort.findByProductId(productId)
                .map(inv -> InventoryResponse.fromEntity(inv, "Tra cứu tồn kho thành công"))
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm trong kho: " + productId));
    }

    @Override
    public InventoryResponse updateStock(UpdateStockRequest request) {
        Inventory inventory = inventoryRepositoryPort.findByProductId(request.getProductId())
                .orElse(Inventory.create(request.getProductId(), 0, 0));

        inventory.updateStock(request.getQuantity());
        Inventory saved = inventoryRepositoryPort.save(inventory);
        log.info("[INVENTORY ADMIN] Cập nhật tồn kho sản phẩm [{}] thành: {}", request.getProductId(), request.getQuantity());
        return InventoryResponse.fromEntity(saved, "Cập nhật tồn kho thành công");
    }

    @Override
    public boolean isInStock(String productId, int quantity) {
        return inventoryRepositoryPort.findByProductId(productId)
                .map(inv -> inv.hasStock(quantity))
                .orElse(false);
    }

    @Override
    public boolean deductInventory(String productId, int quantity) {
        return reserveStock("DIRECT-" + System.currentTimeMillis(), productId, quantity);
    }
}
