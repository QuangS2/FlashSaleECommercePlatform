package com.ecommerce.inventory.application.service;

import com.ecommerce.common.event.BaseEvent;
import com.ecommerce.inventory.domain.entity.Inventory;
import com.ecommerce.inventory.domain.port.out.DistributedLockPort;
import com.ecommerce.inventory.domain.port.out.EventPublisherPort;
import com.ecommerce.inventory.domain.port.out.InventoryRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryApplicationServiceTest {

    @Mock
    private InventoryRepositoryPort inventoryRepositoryPort;

    @Mock
    private DistributedLockPort distributedLockPort;

    @Mock
    private EventPublisherPort eventPublisherPort;

    @InjectMocks
    private InventoryApplicationService inventoryApplicationService;

    @Test
    @SuppressWarnings("unchecked")
    void testReserveStockSuccess() throws Exception {
        when(distributedLockPort.executeWithLock(anyString(), anyLong(), anyLong(), any(Callable.class)))
                .thenAnswer(invocation -> {
                    Callable<Boolean> task = invocation.getArgument(3);
                    return task.call();
                });

        Inventory inventory = Inventory.builder().productId("prod_1").quantity(10).reservedQuantity(0).build();
        when(inventoryRepositoryPort.findByProductId("prod_1")).thenReturn(Optional.of(inventory));
        when(inventoryRepositoryPort.save(any(Inventory.class))).thenAnswer(i -> i.getArgument(0));

        boolean result = inventoryApplicationService.reserveStock("order_1", "prod_1", 2);

        assertTrue(result);
        verify(inventoryRepositoryPort, times(1)).save(any(Inventory.class));
        verify(eventPublisherPort, times(1)).publishInventoryReservedEvent(eq("order_1"), any(BaseEvent.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testReserveStock_OutOfStock() throws Exception {
        when(distributedLockPort.executeWithLock(anyString(), anyLong(), anyLong(), any(Callable.class)))
                .thenAnswer(invocation -> {
                    Callable<Boolean> task = invocation.getArgument(3);
                    return task.call();
                });

        Inventory inventory = Inventory.builder().productId("prod_2").quantity(1).reservedQuantity(0).build();
        when(inventoryRepositoryPort.findByProductId("prod_2")).thenReturn(Optional.of(inventory));

        boolean result = inventoryApplicationService.reserveStock("order_2", "prod_2", 2);

        assertFalse(result);
        verify(inventoryRepositoryPort, never()).save(any(Inventory.class));
        verify(eventPublisherPort, times(1)).publishInventoryReservationFailedEvent(eq("order_2"), any(BaseEvent.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testReserveStock_NotFound() throws Exception {
        when(distributedLockPort.executeWithLock(anyString(), anyLong(), anyLong(), any(Callable.class)))
                .thenAnswer(invocation -> {
                    Callable<Boolean> task = invocation.getArgument(3);
                    return task.call();
                });

        when(inventoryRepositoryPort.findByProductId("prod_3")).thenReturn(Optional.empty());

        boolean result = inventoryApplicationService.reserveStock("order_3", "prod_3", 1);

        assertFalse(result);
        verify(inventoryRepositoryPort, never()).save(any(Inventory.class));
        verify(eventPublisherPort, times(1)).publishInventoryReservationFailedEvent(eq("order_3"), any(BaseEvent.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testReserveStock_LockTimeout() throws Exception {
        when(distributedLockPort.executeWithLock(anyString(), anyLong(), anyLong(), any(Callable.class)))
                .thenReturn(null);

        boolean result = inventoryApplicationService.reserveStock("order_4", "prod_4", 1);

        assertFalse(result);
        verify(inventoryRepositoryPort, never()).findByProductId(anyString());
        verify(eventPublisherPort, times(1)).publishInventoryReservationFailedEvent(eq("order_4"), any(BaseEvent.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testReserveStock_Exception() throws Exception {
        when(distributedLockPort.executeWithLock(anyString(), anyLong(), anyLong(), any(Callable.class)))
                .thenThrow(new RuntimeException("Redis connection error"));

        boolean result = inventoryApplicationService.reserveStock("order_5", "prod_5", 1);

        assertFalse(result);
        verify(eventPublisherPort, times(1)).publishInventoryReservationFailedEvent(eq("order_5"), any(BaseEvent.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testRestoreStockSuccess() throws Exception {
        when(distributedLockPort.executeWithLock(anyString(), anyLong(), anyLong(), any(Callable.class)))
                .thenAnswer(invocation -> {
                    Callable<Boolean> task = invocation.getArgument(3);
                    return task.call();
                });

        Inventory inventory = Inventory.builder().productId("prod_1").quantity(8).reservedQuantity(2).build();
        when(inventoryRepositoryPort.findByProductId("prod_1")).thenReturn(Optional.of(inventory));
        when(inventoryRepositoryPort.save(any(Inventory.class))).thenAnswer(i -> i.getArgument(0));

        boolean result = inventoryApplicationService.restoreStock("order_1", "prod_1", 2, "Payment failed");

        assertTrue(result);
        verify(inventoryRepositoryPort, times(1)).save(any(Inventory.class));
        verify(eventPublisherPort, times(1)).publishInventoryRestoredEvent(eq("order_1"), any(BaseEvent.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testRestoreStock_NotFound() throws Exception {
        when(distributedLockPort.executeWithLock(anyString(), anyLong(), anyLong(), any(Callable.class)))
                .thenAnswer(invocation -> {
                    Callable<Boolean> task = invocation.getArgument(3);
                    return task.call();
                });

        when(inventoryRepositoryPort.findByProductId("prod_1")).thenReturn(Optional.empty());

        boolean result = inventoryApplicationService.restoreStock("order_1", "prod_1", 2, "Payment failed");

        assertFalse(result);
        verify(inventoryRepositoryPort, never()).save(any(Inventory.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testRestoreStock_LockTimeout() throws Exception {
        when(distributedLockPort.executeWithLock(anyString(), anyLong(), anyLong(), any(Callable.class)))
                .thenReturn(null);

        boolean result = inventoryApplicationService.restoreStock("order_1", "prod_1", 2, "Payment failed");

        assertFalse(result);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testRestoreStock_Exception() throws Exception {
        when(distributedLockPort.executeWithLock(anyString(), anyLong(), anyLong(), any(Callable.class)))
                .thenThrow(new RuntimeException("Redis connection error"));

        boolean result = inventoryApplicationService.restoreStock("order_1", "prod_1", 2, "Payment failed");

        assertFalse(result);
    }

    @Test
    void testGetInventoryByProductId_Success() {
        Inventory inventory = Inventory.builder().productId("prod_1").quantity(10).reservedQuantity(0).build();
        when(inventoryRepositoryPort.findByProductId("prod_1")).thenReturn(Optional.of(inventory));

        com.ecommerce.inventory.dto.InventoryResponse response = inventoryApplicationService.getInventoryByProductId("prod_1");

        org.junit.jupiter.api.Assertions.assertNotNull(response);
        org.junit.jupiter.api.Assertions.assertEquals("prod_1", response.getProductId());
        org.junit.jupiter.api.Assertions.assertEquals(10, response.getAvailableQuantity());
    }

    @Test
    void testGetInventoryByProductId_NotFound() {
        when(inventoryRepositoryPort.findByProductId("prod_1")).thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            inventoryApplicationService.getInventoryByProductId("prod_1");
        });
    }

    @Test
    void testUpdateStock_Existing() {
        Inventory inventory = Inventory.builder().productId("prod_1").quantity(10).reservedQuantity(0).build();
        when(inventoryRepositoryPort.findByProductId("prod_1")).thenReturn(Optional.of(inventory));
        when(inventoryRepositoryPort.save(any(Inventory.class))).thenAnswer(i -> i.getArgument(0));

        com.ecommerce.inventory.dto.UpdateStockRequest request = com.ecommerce.inventory.dto.UpdateStockRequest.builder()
                .productId("prod_1").quantity(20).build();
        
        com.ecommerce.inventory.dto.InventoryResponse response = inventoryApplicationService.updateStock(request);

        org.junit.jupiter.api.Assertions.assertNotNull(response);
        org.junit.jupiter.api.Assertions.assertEquals(20, response.getAvailableQuantity());
    }

    @Test
    void testUpdateStock_New() {
        when(inventoryRepositoryPort.findByProductId("prod_2")).thenReturn(Optional.empty());
        when(inventoryRepositoryPort.save(any(Inventory.class))).thenAnswer(i -> i.getArgument(0));

        com.ecommerce.inventory.dto.UpdateStockRequest request = com.ecommerce.inventory.dto.UpdateStockRequest.builder()
                .productId("prod_2").quantity(50).build();

        com.ecommerce.inventory.dto.InventoryResponse response = inventoryApplicationService.updateStock(request);

        org.junit.jupiter.api.Assertions.assertNotNull(response);
        org.junit.jupiter.api.Assertions.assertEquals(50, response.getAvailableQuantity());
    }

    @Test
    void testIsInStock_True() {
        Inventory inventory = Inventory.builder().productId("prod_1").quantity(10).reservedQuantity(0).build();
        when(inventoryRepositoryPort.findByProductId("prod_1")).thenReturn(Optional.of(inventory));

        boolean result = inventoryApplicationService.isInStock("prod_1", 5);

        assertTrue(result);
    }

    @Test
    void testIsInStock_False() {
        Inventory inventory = Inventory.builder().productId("prod_1").quantity(10).reservedQuantity(0).build();
        when(inventoryRepositoryPort.findByProductId("prod_1")).thenReturn(Optional.of(inventory));

        boolean result = inventoryApplicationService.isInStock("prod_1", 15);

        assertFalse(result);
    }

    @Test
    void testIsInStock_NotFound() {
        when(inventoryRepositoryPort.findByProductId("prod_1")).thenReturn(Optional.empty());

        boolean result = inventoryApplicationService.isInStock("prod_1", 5);

        assertFalse(result);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testDeductInventory() throws Exception {
        when(distributedLockPort.executeWithLock(anyString(), anyLong(), anyLong(), any(Callable.class)))
                .thenAnswer(invocation -> {
                    Callable<Boolean> task = invocation.getArgument(3);
                    return task.call();
                });

        Inventory inventory = Inventory.builder().productId("prod_1").quantity(10).reservedQuantity(0).build();
        when(inventoryRepositoryPort.findByProductId("prod_1")).thenReturn(Optional.of(inventory));
        when(inventoryRepositoryPort.save(any(Inventory.class))).thenAnswer(i -> i.getArgument(0));

        boolean result = inventoryApplicationService.deductInventory("prod_1", 2);

        assertTrue(result);
    }
}
