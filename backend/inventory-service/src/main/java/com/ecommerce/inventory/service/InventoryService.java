package com.ecommerce.inventory.service;

import com.ecommerce.common.config.KafkaTopicConstants;
import com.ecommerce.common.event.BaseEvent;
import com.ecommerce.common.event.EventType;
import com.ecommerce.common.event.inventory.InventoryReservationFailedEvent;
import com.ecommerce.common.event.inventory.InventoryReservedEvent;
import com.ecommerce.common.event.inventory.InventoryRestoredEvent;
import com.ecommerce.common.kafka.EventPublisherService;
import com.ecommerce.inventory.dto.InventoryResponse;
import com.ecommerce.inventory.dto.UpdateStockRequest;
import com.ecommerce.inventory.model.Inventory;
import com.ecommerce.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    private final InventoryRepository inventoryRepository;
    private final RedissonClient redissonClient;
    private final EventPublisherService eventPublisherService;

    /**
     * Giữ chỗ kho (Inventory Reservation) được bảo vệ bởi Redisson Distributed Lock.
     */
    public boolean reserveStock(String orderId, String productId, int quantity) {
        String lockKey = "inventory:lock:" + productId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // Thử lấy Lock trong tối đa 5s, giữ lock tối đa 5s
            boolean isLocked = lock.tryLock(5, 5, TimeUnit.SECONDS);
            if (!isLocked) {
                log.warn("[INVENTORY LOCK TIMEOUT] Không thể lấy lock cho sản phẩm [{}] đơn [{}]", productId, orderId);
                publishReservationFailed(orderId, productId, quantity, "Hệ thống đang quá tải, không thể lấy khóa kho");
                return false;
            }

            try {
                Optional<Inventory> optInv = inventoryRepository.findByProductId(productId);
                if (optInv.isPresent() && optInv.get().getQuantity() >= quantity) {
                    Inventory inventory = optInv.get();
                    inventory.setQuantity(inventory.getQuantity() - quantity);
                    int reserved = (inventory.getReservedQuantity() != null ? inventory.getReservedQuantity() : 0) + quantity;
                    inventory.setReservedQuantity(reserved);
                    inventoryRepository.save(inventory);

                    log.info("[INVENTORY SUCCESS] Đã giữ chỗ thành công {} sản phẩm [{}] cho đơn hàng [{}]. Tồn kho còn lại: {}",
                            quantity, productId, orderId, inventory.getQuantity());

                    // Phát sự kiện INVENTORY_RESERVED
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

                    eventPublisherService.publish(KafkaTopicConstants.TOPIC_INVENTORY_EVENTS, orderId, event);
                    return true;
                } else {
                    int available = optInv.map(Inventory::getQuantity).orElse(0);
                    log.warn("[INVENTORY OUT_OF_STOCK] Sản phẩm [{}] không đủ hàng (Yêu cầu: {}, Tồn kho: {}) cho đơn [{}]",
                            productId, quantity, available, orderId);

                    publishReservationFailed(orderId, productId, quantity, "Sản phẩm đã hết hàng tồn kho Flash Sale");
                    return false;
                }
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[INVENTORY ERROR] Bị gián đoạn khi chờ lock cho sản phẩm [{}]: {}", productId, e.getMessage());
            publishReservationFailed(orderId, productId, quantity, "Lỗi gián đoạn luồng xử lý kho");
            return false;
        }
    }

    /**
     * Giao dịch Bù trừ (Compensating Transaction): Hoàn trả lại số lượng kho khi đơn hàng bị huỷ.
     */
    public boolean restoreStock(String orderId, String productId, int quantity, String reason) {
        String lockKey = "inventory:lock:" + productId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean isLocked = lock.tryLock(5, 5, TimeUnit.SECONDS);
            if (!isLocked) {
                log.warn("[INVENTORY RESTORE TIMEOUT] Không thể lấy lock hoàn kho cho sản phẩm [{}] đơn [{}]", productId, orderId);
                return false;
            }

            try {
                Optional<Inventory> optInv = inventoryRepository.findByProductId(productId);
                if (optInv.isPresent()) {
                    Inventory inventory = optInv.get();
                    inventory.setQuantity(inventory.getQuantity() + quantity);
                    int reserved = (inventory.getReservedQuantity() != null ? inventory.getReservedQuantity() : 0);
                    if (reserved >= quantity) {
                        inventory.setReservedQuantity(reserved - quantity);
                    }
                    inventoryRepository.save(inventory);

                    log.info("[INVENTORY COMPENSATED] Đã hoàn trả {} sản phẩm [{}] cho đơn [{}] (Lý do: {}). Tồn kho mới: {}",
                            quantity, productId, orderId, reason, inventory.getQuantity());

                    // Phát sự kiện INVENTORY_RESTORED
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

                    eventPublisherService.publish(KafkaTopicConstants.TOPIC_INVENTORY_EVENTS, orderId, event);
                    return true;
                }
                return false;
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("[INVENTORY RESTORE ERROR] Bị gián đoạn khi hoàn kho cho sản phẩm [{}]: {}", productId, e.getMessage());
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

        eventPublisherService.publish(KafkaTopicConstants.TOPIC_INVENTORY_EVENTS, orderId, event);
    }

    @Transactional(readOnly = true)
    public InventoryResponse getInventoryByProductId(String productId) {
        return inventoryRepository.findByProductId(productId)
                .map(inv -> InventoryResponse.fromEntity(inv, "Tra cứu tồn kho thành công"))
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm trong kho: " + productId));
    }

    @Transactional
    public InventoryResponse updateStock(UpdateStockRequest request) {
        Inventory inventory = inventoryRepository.findByProductId(request.getProductId())
                .orElse(Inventory.builder()
                        .productId(request.getProductId())
                        .quantity(0)
                        .reservedQuantity(0)
                        .build());

        inventory.setQuantity(request.getQuantity());
        Inventory saved = inventoryRepository.save(inventory);
        log.info("[INVENTORY ADMIN] Cập nhật tồn kho sản phẩm [{}] thành: {}", request.getProductId(), request.getQuantity());
        return InventoryResponse.fromEntity(saved, "Cập nhật tồn kho thành công");
    }

    @Transactional(readOnly = true)
    public boolean isInStock(String productId, int quantity) {
        return inventoryRepository.findByProductId(productId)
                .map(inv -> inv.getQuantity() >= quantity)
                .orElse(false);
    }

    @Transactional
    public Inventory saveInventory(Inventory inventory) {
        return inventoryRepository.save(inventory);
    }

    public boolean deductInventory(String productId, int quantity) {
        return reserveStock("DIRECT-" + System.currentTimeMillis(), productId, quantity);
    }
}
