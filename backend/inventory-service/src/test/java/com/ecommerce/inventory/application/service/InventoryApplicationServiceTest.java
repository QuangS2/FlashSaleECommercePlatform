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
}
