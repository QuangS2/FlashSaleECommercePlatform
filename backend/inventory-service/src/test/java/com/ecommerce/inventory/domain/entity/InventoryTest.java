package com.ecommerce.inventory.domain.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InventoryTest {

    @Test
    void testReserveSuccess() {
        Inventory inventory = Inventory.builder()
                .productId("PROD_1")
                .quantity(10)
                .reservedQuantity(0)
                .build();

        boolean result = inventory.reserve(3);
        assertTrue(result);
        assertEquals(7, inventory.getQuantity());
        assertEquals(3, inventory.getReservedQuantity());
    }

    @Test
    void testReserveInsufficientStock() {
        Inventory inventory = Inventory.builder()
                .productId("PROD_1")
                .quantity(2)
                .reservedQuantity(0)
                .build();

        boolean result = inventory.reserve(3);
        assertFalse(result);
        assertEquals(2, inventory.getQuantity());
    }

    @Test
    void testReserveInvalidQuantity() {
        Inventory inventory = Inventory.builder()
                .productId("PROD_1")
                .quantity(10)
                .reservedQuantity(0)
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                inventory.reserve(-1));
        assertEquals("Reservation amount must be greater than zero", exception.getMessage());
    }

    @Test
    void testRestoreStock() {
        Inventory inventory = Inventory.builder()
                .productId("PROD_1")
                .quantity(5)
                .reservedQuantity(2)
                .build();

        inventory.restore(2);
        assertEquals(7, inventory.getQuantity());
        assertEquals(0, inventory.getReservedQuantity());
    }

    @Test
    void testRestoreInvalidQuantity() {
        Inventory inventory = Inventory.builder()
                .productId("PROD_1")
                .quantity(5)
                .reservedQuantity(0)
                .build();

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                inventory.restore(-5));
        assertEquals("Restore amount must be greater than zero", exception.getMessage());
    }

    @Test
    void testCreate() {
        Inventory inventory = Inventory.create("PROD_2", 10, 5);
        assertNotNull(inventory);
        assertEquals("PROD_2", inventory.getProductId());
        assertEquals(10, inventory.getQuantity());
        assertEquals(5, inventory.getReservedQuantity());
        assertNotNull(inventory.getUpdatedAt());
    }

    @Test
    void testUpdateStock() {
        Inventory inventory = Inventory.builder()
                .productId("PROD_1")
                .quantity(5)
                .build();

        inventory.updateStock(20);
        assertEquals(20, inventory.getQuantity());

        assertThrows(IllegalArgumentException.class, () -> inventory.updateStock(-1));
    }

    @Test
    void testHasStock() {
        Inventory inventory = Inventory.builder()
                .productId("PROD_1")
                .quantity(10)
                .build();

        assertTrue(inventory.hasStock(5));
        assertTrue(inventory.hasStock(10));
        assertFalse(inventory.hasStock(15));
    }

    @Test
    void testGettersAndBuilder() {
        java.time.Instant now = java.time.Instant.now();
        Inventory inventory = Inventory.builder()
                .id(1L)
                .productId("PROD_3")
                .quantity(50)
                .reservedQuantity(10)
                .version(2L)
                .updatedAt(now)
                .build();

        assertEquals(1L, inventory.getId());
        assertEquals("PROD_3", inventory.getProductId());
        assertEquals(50, inventory.getQuantity());
        assertEquals(10, inventory.getReservedQuantity());
        assertEquals(2L, inventory.getVersion());
        assertEquals(now, inventory.getUpdatedAt());
    }
}
