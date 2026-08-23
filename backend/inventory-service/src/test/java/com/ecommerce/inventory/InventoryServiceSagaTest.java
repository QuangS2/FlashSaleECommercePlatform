package com.ecommerce.inventory;

import com.ecommerce.common.config.KafkaTopicConstants;
import com.ecommerce.common.event.BaseEvent;
import com.ecommerce.common.kafka.EventPublisherService;
import com.ecommerce.inventory.dto.InventoryResponse;
import com.ecommerce.inventory.dto.UpdateStockRequest;
import com.ecommerce.inventory.model.Inventory;
import com.ecommerce.inventory.repository.InventoryRepository;
import com.ecommerce.inventory.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InventoryServiceSagaTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private EventPublisherService eventPublisherService;

    @Mock
    private RLock mockLock;

    @InjectMocks
    private InventoryService inventoryService;

    @BeforeEach
    public void setup() {
        lenient().when(redissonClient.getLock(anyString())).thenReturn(mockLock);
    }

    @Test
    @DisplayName("Test 1: reserveStock - Sufficient stock acquires lock, decrements stock, and publishes InventoryReservedEvent")
    public void testReserveStock_Success() throws InterruptedException {
        String orderId = "ORD-TEST-100";
        String productId = "PROD-IPHONE-15";
        int requestedQty = 2;

        Inventory existingInventory = Inventory.builder()
                .productId(productId)
                .quantity(10)
                .reservedQuantity(0)
                .build();

        when(mockLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(mockLock.isHeldByCurrentThread()).thenReturn(true);
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(existingInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(existingInventory);

        boolean result = inventoryService.reserveStock(orderId, productId, requestedQty);

        assertThat(result).isTrue();
        assertThat(existingInventory.getQuantity()).isEqualTo(8);
        assertThat(existingInventory.getReservedQuantity()).isEqualTo(2);

        verify(mockLock).tryLock(5, 5, TimeUnit.SECONDS);
        verify(inventoryRepository).save(existingInventory);
        verify(eventPublisherService).publish(eq(KafkaTopicConstants.TOPIC_INVENTORY_EVENTS), eq(orderId), any(BaseEvent.class));
        verify(mockLock).unlock();
    }

    @Test
    @DisplayName("Test 2: reserveStock - Out of stock publishes InventoryReservationFailedEvent")
    public void testReserveStock_OutOfStock() throws InterruptedException {
        String orderId = "ORD-TEST-101";
        String productId = "PROD-IPHONE-15";
        int requestedQty = 5;

        Inventory lowStockInventory = Inventory.builder()
                .productId(productId)
                .quantity(2) // Chỉ còn 2 cái trong kho
                .reservedQuantity(0)
                .build();

        when(mockLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(mockLock.isHeldByCurrentThread()).thenReturn(true);
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(lowStockInventory));

        boolean result = inventoryService.reserveStock(orderId, productId, requestedQty);

        assertThat(result).isFalse();
        assertThat(lowStockInventory.getQuantity()).isEqualTo(2); // Không bị trừ

        verify(eventPublisherService).publish(eq(KafkaTopicConstants.TOPIC_INVENTORY_EVENTS), eq(orderId), any(BaseEvent.class));
        verify(mockLock).unlock();
    }

    @Test
    @DisplayName("Test 3: reserveStock - Lock timeout publishes InventoryReservationFailedEvent")
    public void testReserveStock_LockTimeout() throws InterruptedException {
        String orderId = "ORD-TEST-102";
        String productId = "PROD-IPHONE-15";

        when(mockLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(false);

        boolean result = inventoryService.reserveStock(orderId, productId, 1);

        assertThat(result).isFalse();
        verify(eventPublisherService).publish(eq(KafkaTopicConstants.TOPIC_INVENTORY_EVENTS), eq(orderId), any(BaseEvent.class));
        verify(mockLock, never()).unlock();
    }

    @Test
    @DisplayName("Test 4: restoreStock - Compensating transaction restores stock and publishes InventoryRestoredEvent")
    public void testRestoreStock_Success() throws InterruptedException {
        String orderId = "ORD-TEST-103";
        String productId = "PROD-IPHONE-15";
        int restoreQty = 2;

        Inventory inventory = Inventory.builder()
                .productId(productId)
                .quantity(8)
                .reservedQuantity(2)
                .build();

        when(mockLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(mockLock.isHeldByCurrentThread()).thenReturn(true);
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        boolean result = inventoryService.restoreStock(orderId, productId, restoreQty, "Thanh toán thất bại");

        assertThat(result).isTrue();
        assertThat(inventory.getQuantity()).isEqualTo(10);
        assertThat(inventory.getReservedQuantity()).isEqualTo(0);

        verify(inventoryRepository).save(inventory);
        verify(eventPublisherService).publish(eq(KafkaTopicConstants.TOPIC_INVENTORY_EVENTS), eq(orderId), any(BaseEvent.class));
        verify(mockLock).unlock();
    }

    @Test
    @DisplayName("Test 5: getInventoryByProductId - Returns inventory details")
    public void testGetInventoryByProductId() {
        String productId = "PROD-1";
        Inventory inventory = Inventory.builder().productId(productId).quantity(50).reservedQuantity(5).build();
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(inventory));

        InventoryResponse response = inventoryService.getInventoryByProductId(productId);
        assertThat(response.getProductId()).isEqualTo(productId);
        assertThat(response.getAvailableQuantity()).isEqualTo(50);

        when(inventoryRepository.findByProductId("PROD-NOT-EXIST")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> inventoryService.getInventoryByProductId("PROD-NOT-EXIST"));
    }

    @Test
    @DisplayName("Test 6: updateStock - Saves updated stock quantity")
    public void testUpdateStock() {
        UpdateStockRequest request = UpdateStockRequest.builder()
                .productId("PROD-1")
                .quantity(200)
                .build();

        when(inventoryRepository.findByProductId("PROD-1")).thenReturn(Optional.empty());
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(i -> i.getArgument(0));

        InventoryResponse response = inventoryService.updateStock(request);
        assertThat(response.getProductId()).isEqualTo("PROD-1");
        assertThat(response.getAvailableQuantity()).isEqualTo(200);
        verify(inventoryRepository).save(any(Inventory.class));
    }
}
