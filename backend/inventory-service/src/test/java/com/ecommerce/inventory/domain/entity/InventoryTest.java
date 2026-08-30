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
}
